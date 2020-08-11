/**
 * @author  Alon Eirew
 */

package wiki;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.relations.*;
import wiki.elastic.ElasticAPI;
import wiki.handlers.ElasticPageHandler;
import wiki.handlers.IPageHandler;
import wiki.utils.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

public class WikiToElasticMain {

    private final static Logger LOGGER = LogManager.getLogger(WikiToElasticMain.class);
    private static final Gson GSON = new Gson();

    public static void main(String[] args) {
        try {
            LOGGER.info("Initiating all resources...");
            WikiToElasticConfiguration config = GSON.fromJson(new FileReader("conf.json"), WikiToElasticConfiguration.class);
            InputStream inputStream = Objects.requireNonNull(WikiToElasticMain.class.getClassLoader().getResourceAsStream("lang/" + config.getLang() + ".json"));
            LangConfiguration langConfiguration = GSON.fromJson(new InputStreamReader(inputStream, StandardCharsets.UTF_8), LangConfiguration.class);

            ExtractorsManager.initExtractors(config, langConfiguration);
            LOGGER.info("Process configuration loaded");

            long startTime = System.currentTimeMillis();
            startProcess(config, langConfiguration);
            long endTime = System.currentTimeMillis();

            long durationInMillis = endTime - startTime;
            long took = TimeUnit.MILLISECONDS.toMinutes(durationInMillis);
            LOGGER.info("Process Done, took~" + took + "min (" + durationInMillis + "ms)" );
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to start process", e);
        } catch (IOException e) {
            LOGGER.error("I/O Error", e);
        } catch (Exception e) {
            LOGGER.error("Something went wrong..", e);
        }
    }

    /**
     * Start the main process of parsing the wikipedia dump file, create resources and handlers for executing the task
     * @param configuration <a href="https://github.com/AlonEirew/wikipedia-to-elastic/blob/master/conf.json">conf.json file</a>
     * @throws IOException
     */
    static void startProcess(WikiToElasticConfiguration configuration, LangConfiguration langConfiguration) throws IOException {
        InputStream inputStream = null;
        STAXParser parser = null;
        IPageHandler pageHandler = null;
        ElasticAPI elasicApi = null;
        STAXParser.DeleteUpdateMode mode;
        try(Scanner reader = new Scanner(System.in)) {
            LOGGER.info("Reading wikidump: " + configuration.getWikiDump());
            File wikifile = new File(configuration.getWikiDump());
            if(wikifile.exists()) {
                inputStream = WikiToElasticUtils.openCompressedFileInputStream(wikifile.getPath());
                elasicApi = new ElasticAPI(configuration);

                // Delete if index already exists
                System.out.println("Would you like to clean & delete index (if exists) \"" + configuration.getIndexName() +
                        "\" or update (new pages) in it [D(Delete)/U(Update)]");

                // Scans the next token of the input as an int.
                String ans = reader.nextLine();

                if(ans.equalsIgnoreCase("d") || ans.equalsIgnoreCase("delete")) {
                    elasicApi.deleteIndex(configuration.getIndexName());

                    // Create the elastic search index
                    elasicApi.createIndex(configuration);
                    mode = STAXParser.DeleteUpdateMode.DELETE;
                } else if(ans.equalsIgnoreCase("u") || ans.equalsIgnoreCase("update")) {
                    if(!elasicApi.isIndexExists(configuration.getIndexName())) {
                        LOGGER.info("Index \"" + configuration.getIndexName() +
                                "\" not found, exit application.");
                        return;
                    }

                    mode = STAXParser.DeleteUpdateMode.UPDATE;
                } else {
                    return;
                }

                // Start parsing the xml and adding pages to elastic
                pageHandler = new ElasticPageHandler(elasicApi, configuration);

                parser = new STAXParser(pageHandler, configuration, langConfiguration, mode);
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
                parser.shutDownPool();
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
}
