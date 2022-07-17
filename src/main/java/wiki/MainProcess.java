package wiki;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.config.ElasticConfiguration;
import wiki.config.JsonFileConfiguration;
import wiki.handlers.ElasticPageHandler;
import wiki.handlers.IPageHandler;
import wiki.persistency.ElasticAPI;
import wiki.config.LangConfiguration;
import wiki.config.MainConfiguration;
import wiki.utils.WikiToElasticUtils;
import wiki.utils.parsers.WikipediaSTAXParser;

import java.io.*;
import java.util.Scanner;

public class MainProcess {
    private final static Logger LOGGER = LogManager.getLogger(WikiToElasticMain.class);
    private static final Gson GSON = new Gson();

    private final String exportMethod;
    private final MainConfiguration mainConfiguration;
    private final LangConfiguration langConfiguration;
    private ElasticConfiguration elasticConfiguration = null;
    private JsonFileConfiguration jsonFileConfiguration = null;

    public MainProcess(MainConfiguration mainConfiguration, LangConfiguration langConfiguration) throws FileNotFoundException {
        this.mainConfiguration = mainConfiguration;
        this.langConfiguration = langConfiguration;
        this.exportMethod = this.mainConfiguration.getExportMethod();
        if (this.exportMethod.equalsIgnoreCase("elastic")) {
            elasticConfiguration = GSON.fromJson(new FileReader("config/elastic_conf.json"), ElasticConfiguration.class);
        } else if(this.exportMethod.equalsIgnoreCase("json_file")) {
            jsonFileConfiguration = GSON.fromJson(new FileReader("config/json_file_conf.json"), JsonFileConfiguration.class);
        }
    }

    public void startProcess() throws IOException {
        if (this.exportMethod.equalsIgnoreCase("elastic")) {
            this.startElasticProcess();
        } else if(this.exportMethod.equalsIgnoreCase("json_file")) {
            this.startJsonProcess();
        }
    }

    /**
     * Start the main process of parsing the wikipedia dump file, create resources and handlers for executing the task
     * @throws IOException
     */
    private void startElasticProcess() throws IOException {
        if (this.elasticConfiguration == null) {
            throw new IOException("Elastic configuration not initialized!");
        }

        InputStream inputStream = null;
        WikipediaSTAXParser parser = null;
        IPageHandler pageHandler = null;
        ElasticAPI elasicApi = null;
        WikipediaSTAXParser.DeleteUpdateMode mode;
        try(Scanner reader = new Scanner(System.in)) {
            LOGGER.info("Reading wikidump: " + this.mainConfiguration.getWikipediaDump());
            File wikifile = new File(this.mainConfiguration.getWikipediaDump());
            if(wikifile.exists()) {
                inputStream = WikiToElasticUtils.openCompressedFileInputStream(wikifile.getPath());
                elasicApi = new ElasticAPI(this.elasticConfiguration);

                // Delete if index already exists
                System.out.println("Would you like to clean & delete index (if exists) \"" + this.elasticConfiguration.getIndexName() +
                        "\" or update (new pages) in it [D(Delete)/U(Update)]");

                // Scans the next token of the input as an int.
                String ans = reader.nextLine();

                if(ans.equalsIgnoreCase("d") || ans.equalsIgnoreCase("delete")) {
                    elasicApi.deleteIndex();

                    // Create the elastic search index
                    elasicApi.createIndex(this.elasticConfiguration);
                    mode = WikipediaSTAXParser.DeleteUpdateMode.DELETE;
                } else if(ans.equalsIgnoreCase("u") || ans.equalsIgnoreCase("update")) {
                    if(!elasicApi.isIndexExists()) {
                        LOGGER.info("Index \"" + this.elasticConfiguration.getIndexName() +
                                "\" not found, exit application.");
                        return;
                    }

                    mode = WikipediaSTAXParser.DeleteUpdateMode.UPDATE;
                } else {
                    return;
                }

                // Start parsing the xml and adding pages to elastic
                pageHandler = new ElasticPageHandler(elasicApi, this.elasticConfiguration.getInsertBulkSize());

                parser = new WikipediaSTAXParser(pageHandler, this.mainConfiguration, this.langConfiguration, mode);
                parser.parse(inputStream);
            } else {
                LOGGER.error("Cannot find dump file-" + wikifile.getAbsolutePath());
            }

        } catch (IOException ex) {
            LOGGER.error("Export Failed!", ex);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
            if(parser != null) {
                parser.close();
                pageHandler.close();
                LOGGER.info("*** Total id's extracted=" + parser.getTotalIds().size());
                LOGGER.info("*** In commit queue=" + ((ElasticPageHandler) pageHandler).getPagesQueueSize() + " (should be 0)");
            }
            if(elasicApi != null) {
                elasicApi.close();
                LOGGER.info("*** Total id's committed=" + elasicApi.getTotalIdsSuccessfullyCommitted());
            }
        }
    }

    private void startJsonProcess() throws IOException {
        if (this.jsonFileConfiguration == null) {
            throw new IOException("Json file configuration not initialized!");
        }
    }
}

