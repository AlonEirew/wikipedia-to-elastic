/**
 * @author  Alon Eirew
 */

package wiki;

import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import wiki.data.relations.BeCompRelationExtractor;
import wiki.data.relations.CategoryRelationExtractor;
import wiki.elastic.ElasticAPI;
import wiki.handlers.ElasticPageHandler;
import wiki.handlers.IPageHandler;
import wiki.parsers.STAXParser;
import wiki.utils.LangConfiguration;
import wiki.utils.WikiPageParser;
import wiki.utils.WikiToElasticConfiguration;
import wiki.utils.WikiToElasticUtils;

import java.io.*;
import java.util.Objects;
import java.util.Scanner;

public class WikiToElasticMain {

    private final static Logger LOGGER = LogManager.getLogger(WikiToElasticMain.class);
    private static final Gson GSON = new Gson();

    public static void main(String[] args) {
        try {
            WikiToElasticConfiguration config = GSON.fromJson(new FileReader("conf.json"), WikiToElasticConfiguration.class);
            String langConfigFile = Objects.requireNonNull(WikiToElasticMain.class.getClassLoader().getResource("lang/" + config.getLang() + ".json")).getFile();
            LangConfiguration langConfiguration = GSON.fromJson(new FileReader(langConfigFile), LangConfiguration.class);

            WikiPageParser.initResources(langConfiguration, config.getLang());
            BeCompRelationExtractor.initResources(langConfiguration);
            CategoryRelationExtractor.initResources(langConfiguration);
            LOGGER.info("Process configuration loaded");

            long startTime = System.currentTimeMillis();
            startProcess(config, langConfiguration);
            long endTime = System.currentTimeMillis();

            LOGGER.info("Process Done, took:" + (endTime - startTime) + "ms");
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
        RestHighLevelClient client = null;
        InputStream inputStream = null;
        STAXParser parser = null;
        IPageHandler pageHandler = null;
        STAXParser.DeleteUpdateMode mode;
        try(Scanner reader = new Scanner(System.in)) {
            LOGGER.info("Reading wikidump: " + configuration.getWikiDump());
            File wikifile = new File(configuration.getWikiDump());

            if(wikifile.exists()) {
                inputStream = WikiToElasticUtils.openCompressedFileInputStream(wikifile.getPath());
                // init elastic client
                client = new RestHighLevelClient(
                        RestClient.builder(
                                new HttpHost(configuration.getHost(),
                                        configuration.getPort(),
                                        configuration.getScheme())));

                ElasticAPI elasicApi = new ElasticAPI(client);

                // Delete if index already exists
                System.out.println("Would you like to clean & delete index (if exists) \"" + configuration.getIndexName() +
                        "\" or update (new pages) in it [D(Delete)/U(Update)]");

                String ans = reader.nextLine(); // Scans the next token of the input as an int.
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
                LOGGER.info("*** Total id's extracted=" + parser.getTotalIds().size());
                parser.shutDownPool();
                pageHandler.close();
                LOGGER.info("*** Total id's committed=" + ((ElasticPageHandler) pageHandler).getTotalIdsCommitted());
                LOGGER.info("*** In commit queue=" + ((ElasticPageHandler) pageHandler).getPagesQueueSize() + " (should be 0)");
            }
            if(client != null) {
                LOGGER.info("Closing RestHighLevelClient..");
                client.close();
            }
        }
    }
}
