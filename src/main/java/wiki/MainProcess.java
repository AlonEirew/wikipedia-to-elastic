package wiki;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.config.ElasticConfiguration;
import wiki.config.JsonFileConfiguration;
import wiki.handlers.PageHandler;
import wiki.handlers.IPageHandler;
import wiki.persistency.ElasticAPI;
import wiki.config.LangConfiguration;
import wiki.config.MainConfiguration;
import wiki.persistency.IAPI;
import wiki.persistency.JsonFileAPI;
import wiki.utils.WikiToElasticUtils;
import wiki.utils.parsers.WikipediaSTAXParser;

import java.io.*;
import java.util.Scanner;

public class MainProcess {
    private static final String ELASTIC = "elastic";
    private static final String JSON = "json_files";

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
        if (this.exportMethod.equalsIgnoreCase(ELASTIC)) {
            elasticConfiguration = GSON.fromJson(new FileReader("config/elastic_conf.json"), ElasticConfiguration.class);
        } else if(this.exportMethod.equalsIgnoreCase(JSON)) {
            jsonFileConfiguration = GSON.fromJson(new FileReader("config/json_file_conf.json"), JsonFileConfiguration.class);
        }
    }

    public void startProcess() throws IOException {
        if (this.exportMethod.equalsIgnoreCase(ELASTIC)) {
            ElasticAPI elasicApi = new ElasticAPI(this.elasticConfiguration);
            String indexName = this.elasticConfiguration.getIndexName();
            int bulkSize = this.elasticConfiguration.getInsertBulkSize();
            this.runProcess(elasicApi, indexName, bulkSize);
        } else if(this.exportMethod.equalsIgnoreCase(JSON)) {
            JsonFileAPI fileApi = new JsonFileAPI(this.jsonFileConfiguration.getOutIndexDirectory());
            String indexDirectory = this.jsonFileConfiguration.getOutIndexDirectory();
            int pagesPerFile = this.jsonFileConfiguration.getPagesPerFile();
            this.runProcess(fileApi, indexDirectory, pagesPerFile);
        } else {
            throw new IllegalArgumentException("Invalid exportMethod argument=" + this.exportMethod);
        }
    }

    /**
     * Start the main process of parsing the wikipedia dump file, create resources and handlers for executing the task
     * @throws IOException
     */
    private <T> void runProcess(IAPI<T> api, String indexName, int bulkSize) throws IOException {
        InputStream inputStream = null;
        WikipediaSTAXParser parser = null;
        IPageHandler pageHandler = null;
        WikipediaSTAXParser.DeleteUpdateMode mode;
        try(Scanner reader = new Scanner(System.in)) {
            LOGGER.info("Reading wikidump: " + this.mainConfiguration.getWikipediaDump());
            File wikifile = new File(this.mainConfiguration.getWikipediaDump());
            if(wikifile.exists()) {
                inputStream = WikiToElasticUtils.openCompressedFileInputStream(wikifile.getPath());

                // Delete if index already exists
                System.out.println("Would you like to clean & delete index (if exists) \"" + indexName +
                        "\" or update (new pages) in it [D(Delete)/U(Update)]");

                // Scans the next token of the input as an int.
                String ans = reader.nextLine();

                if(ans.equalsIgnoreCase("d") || ans.equalsIgnoreCase("delete")) {
                    api.deleteIndex();

                    // Create the elastic search index
                    api.createIndex();
                    mode = WikipediaSTAXParser.DeleteUpdateMode.DELETE;
                } else if(ans.equalsIgnoreCase("u") || ans.equalsIgnoreCase("update")) {
                    if(!api.isIndexExists()) {
                        LOGGER.info("Index \"" + indexName +
                                "\" not found, exit application.");
                        return;
                    }

                    mode = WikipediaSTAXParser.DeleteUpdateMode.UPDATE;
                } else {
                    return;
                }

                // Start parsing the xml and adding pages to elastic
                pageHandler = new PageHandler(api, bulkSize);

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
                LOGGER.info("*** In commit queue=" + ((PageHandler) pageHandler).getPagesQueueSize() + " (should be 0)");
            }
            if(api != null) {
                api.close();
                LOGGER.info("*** Total id's committed=" + api.getTotalIdsSuccessfullyCommitted());
            }
        }
    }
}

