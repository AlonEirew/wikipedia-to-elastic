package wiki.wikidata;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.config.ElasticConfiguration;
import wiki.data.WikiDataParsedPage;
import wiki.data.WikipediaParsedPage;
import wiki.persistency.ElasticAPI;
import wiki.utils.SimpleExecutorService;
import wiki.config.MainConfiguration;
import wiki.utils.WikiToElasticUtils;
import wiki.utils.parsers.WikidataJsonParser;
import wiki.utils.parsers.WikidataParseThread;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;

public class WikiDataFeatToFile {
    private final static Logger LOGGER = LogManager.getLogger(WikiDataFeatToFile.class);
    private final static Gson GSON = new Gson();

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        MainConfiguration mainConfiguration = GSON.fromJson(new FileReader("conf.json"), MainConfiguration.class);
        ElasticConfiguration elasticConfiguration = GSON.fromJson(new FileReader("config/wikidata_conf.json"), ElasticConfiguration.class);

        if(mainConfiguration.getWikidataDump() != null && !mainConfiguration.getWikidataDump().isEmpty()) {
            File inputDump = new File(mainConfiguration.getWikidataDump());
            File outputFile = new File(mainConfiguration.getWikidataJsonOutput());
            ElasticAPI elasticAPI = new ElasticAPI(elasticConfiguration);
            try (Scanner reader = new Scanner(System.in)) {
                System.out.println("This process will extract Wikidata information to json file-\"" +
                        mainConfiguration.getWikidataJsonOutput() + "\",\nPress enter to continue...");
                reader.nextLine();

                final WikidataJsonParser wikidataJsonParser = new WikidataJsonParser();
                List<WikiDataParsedPage> wikiDataParsedPages = generateWikidata(inputDump, elasticAPI, wikidataJsonParser, mainConfiguration.getLang());
                writeRelationToJson(outputFile, wikidataJsonParser, wikiDataParsedPages);
                LOGGER.info("*** Total id's extracted=" + wikiDataParsedPages.size());
            } catch (IOException | ExecutionException | InterruptedException e) {
                LOGGER.error("Something went wrong!", e);
            }

            elasticAPI.close();
            long endTime = System.currentTimeMillis();
            System.out.println("Done, took-" + (endTime - startTime) + "ms");
        } else {
            LOGGER.error("Wikidata dump file not set in configuration");
        }
    }

    private static List<WikiDataParsedPage> generateWikidata(final File inputDump, final ElasticAPI elasticAPI,
             WikidataJsonParser wikidataJsonParser, String lang) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            WikidataParseThread parseThread = new WikidataParseThread(lang);
            InputStreamReader inputStreamReader = new InputStreamReader(WikiToElasticUtils.openCompressedFileInputStream(inputDump.getPath()), StandardCharsets.UTF_8);
            Callable<Map<String, WikiDataParsedPage>> wikidataParseRun = () -> wikidataJsonParser.parse(parseThread, inputStreamReader);
            Future<Map<String, WikiDataParsedPage>> wikidataParseSubmit = executor.submit(wikidataParseRun);

            Callable<Map<String, WikipediaParsedPage>> wikipediaRun = () -> elasticAPI.readAllWikipediaIdsTitles(-1);
            Future<Map<String, WikipediaParsedPage>> elasticReadIdsSubmit = executor.submit(wikipediaRun);

            Map<String, WikiDataParsedPage> parsedPagesWikidata = wikidataParseSubmit.get();
            Map<String, WikipediaParsedPage> wikipediaTitleToId = elasticReadIdsSubmit.get();

            SimpleExecutorService.shutDownPool(executor);
            return WikiDataParsedPage.prepareWikipediaWikidataMergeList(parsedPagesWikidata, wikipediaTitleToId);
        } catch (Exception ex) {
            LOGGER.error("Failed processing parsers to wikipedia", ex);
            SimpleExecutorService.shutDownPoolNow(executor);
            throw ex;
        }
    }

    private static void writeRelationToJson(File outputFile, WikidataJsonParser wikidataJsonParser, List<WikiDataParsedPage> wikiDataParsedPages) throws IOException {
        if(wikiDataParsedPages != null) {
            wikidataJsonParser.write(outputFile, wikiDataParsedPages);
//            ListUtils.partition(wikiDataParsedPages, bulkSize).forEach(elasticAPI::updateBulkWikidataAsnc);
        }
    }
}
