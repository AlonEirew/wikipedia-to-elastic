package wiki;

import com.google.gson.Gson;
import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.WikiDataParsedPage;
import wiki.data.WikipediaParsedPage;
import wiki.elastic.ElasticAPI;
import wiki.utils.SimpleExecutorService;
import wiki.utils.WikiToElasticConfiguration;
import wiki.utils.WikidataJsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.*;

public class WikiDataEnrich {
    private final static Logger LOGGER = LogManager.getLogger(WikiDataEnrich.class);
    private final static Gson GSON = new Gson();

    public static void main(String[] args) throws Exception {
        long startTime = System.currentTimeMillis();
        WikiToElasticConfiguration config = GSON.fromJson(new FileReader("wikidata_conf.json"), WikiToElasticConfiguration.class);

        if(config.getWikidataDump() != null && !config.getWikidataDump().isEmpty()) {
            File inputDump = new File(config.getWikidataDump());
            ElasticAPI elasticAPI = new ElasticAPI(config);
            try (Scanner reader = new Scanner(System.in)) {
                System.out.println("This process will integrate Wikidata information within the existing elastic " +
                        "index of \"" + config.getIndexName() + "\",\nPress enter to continue...");
                reader.nextLine();
                List<WikiDataParsedPage> wikiDataParsedPages = generateWikidata(inputDump, elasticAPI, config.getLang());
                updateAll(elasticAPI, wikiDataParsedPages, config.getInsertBulkSize());
            } catch (IOException | ExecutionException | InterruptedException e) {
                LOGGER.error("Something went wrong!", e);
            }

            elasticAPI.close();
            LOGGER.info("*** Total id's committed=" + elasticAPI.getTotalIdsSuccessfullyCommitted());
            long endTime = System.currentTimeMillis();
            System.out.println("Done, took-" + (endTime - startTime) + "ms");
        } else {
            LOGGER.error("Wikidata dump file not set in configuration");
        }
    }

    private static List<WikiDataParsedPage> generateWikidata(final File inputDump, final ElasticAPI elasticAPI, String lang)
            throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            final WikidataJsonParser wikidataJsonParser = new WikidataJsonParser(lang);
            Callable<Map<String, WikiDataParsedPage>> wikidataParseRun = () -> wikidataJsonParser.parse(inputDump);
            Future<Map<String, WikiDataParsedPage>> wikidataParseSubmit = executor.submit(wikidataParseRun);

            Callable<Map<String, WikipediaParsedPage>> wikipediaRun = () -> elasticAPI.readAllWikipediaIdsTitles(-1);
            Future<Map<String, WikipediaParsedPage>> elasticReadIdsSubmit = executor.submit(wikipediaRun);

            Map<String, WikiDataParsedPage> parsedPagesWikidata = wikidataParseSubmit.get();
            Map<String, WikipediaParsedPage> wikipediaTitleToId = elasticReadIdsSubmit.get();

            SimpleExecutorService.shutDownPool(executor);
            return WikiDataParsedPage.prepareWikipediaWikidataMergeList(parsedPagesWikidata, wikipediaTitleToId);
        } catch (Exception ex) {
            LOGGER.error("Failed processing wikidata to wikipedia", ex);
            SimpleExecutorService.shutDownPoolNow(executor);
            throw ex;
        }
    }

    private static void updateAll(ElasticAPI elasticAPI, List<WikiDataParsedPage> wikiDataParsedPages, int bulkSize) throws IOException {
        if(wikiDataParsedPages != null) {
            ListUtils.partition(wikiDataParsedPages, bulkSize).forEach(elasticAPI::updateBulkWikidataAsnc);
        }
    }
}
