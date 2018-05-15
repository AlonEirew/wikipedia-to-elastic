/**
 * @author  Alon Eirew
 */

package wiki;

import com.google.gson.stream.JsonReader;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import wiki.data.ElasticPageHandler;
import wiki.data.IPageHandler;
import wiki.elastic.ElasticAPI;
import wiki.elastic.ElasticBulkDocCreateListener;
import wiki.elastic.IElasticAPI;
import wiki.parsers.STAXParser;
import wiki.utils.WikiToElasticConfiguration;
import wiki.utils.WikiToElasticUtils;

import java.io.*;
import java.net.URL;
import java.util.Scanner;

public class WikiToElasticMain {

    private final static Logger LOGGER = LogManager.getLogger(WikiToElasticMain.class);

    public static void main(String[] args) {
        JsonReader reader = null;
        try {
            LOGGER.info("Starting export Wiki dump to elastic process");
            InputStream resource = WikiToElasticMain.class.getClassLoader().getResourceAsStream("conf.json");
            if(resource != null) {
                reader = new JsonReader(new InputStreamReader(resource));
                WikiToElasticConfiguration configuration = WikiToElasticConfiguration.gson.fromJson(reader, WikiToElasticConfiguration.CONFIGURATION_TYPE);
                LOGGER.info("Process configuration loaded");

                long startTime = System.currentTimeMillis();
                startProcess(configuration);
                long endTime = System.currentTimeMillis();
                LOGGER.info("Process Done, took:" + (endTime - startTime));
            }
        } catch (FileNotFoundException e) {
            LOGGER.error("Failed to start process", e);
        } catch (IOException e) {
            LOGGER.error("I/O Error", e);
        } catch (Exception e) {
            LOGGER.error("Something went wrong..", e);
        } finally {
            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    LOGGER.error("Failed closing JsonReader" , e);
                }
            }
        }

        System.exit(0);
    }

    static void startProcess(WikiToElasticConfiguration configuration) throws IOException {
        RestHighLevelClient client;
        InputStream inputStream = null;
        STAXParser parser = null;
        IPageHandler pageHandler = null;
        try(Scanner reader = new Scanner(System.in);) {
            LOGGER.info("Reading wikidump: " + configuration.getWikiDump());
            File wikifile = new File(configuration.getWikiDump());

            if(wikifile.exists()) {
                inputStream = WikiToElasticUtils.openCompressedFileInputStream(wikifile.getPath());
                // init elastic client
                client = new RestHighLevelClient(
                        RestClient.builder(
                                new HttpHost(configuration.getHost(), configuration.getPort(), configuration.getScheme())));

                IElasticAPI elasicApi = new ElasticAPI(client);

                // Delete if index already exists
                System.out.println("You are about to delete index (if exists) \"" + configuration.getIndexName() + "\" do you want to continue [Y/n]?");
                String ans = reader.nextLine(); // Scans the next token of the input as an int.
                if(ans.equalsIgnoreCase("y") || ans.equalsIgnoreCase("yes")) {
                    elasicApi.deleteIndex(configuration.getIndexName());

                    // Create the index
                    elasicApi.createIndex(configuration);
                    ElasticBulkDocCreateListener listener = new ElasticBulkDocCreateListener();

                    // Start parsing the xml and adding pages to elastic
                    pageHandler = new ElasticPageHandler(elasicApi, listener, configuration);

                    parser = new STAXParser(pageHandler);
                    parser.parse(inputStream);
                }
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
                if(pageHandler != null) {
                    pageHandler.flushRemains();
                }
            }

            LOGGER.info("*** Total id's extracted=" + parser.getTotalIds().size());
            LOGGER.info("*** Total id's committed=" + ((ElasticPageHandler) pageHandler).getTotalIdsCommitted());
            LOGGER.info("*** In commit queue=" + ((ElasticPageHandler) pageHandler).getPagesQueueSize() + "(should be 0)");
        }
    }
}
