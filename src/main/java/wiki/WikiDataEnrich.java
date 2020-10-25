package wiki;

import com.google.gson.Gson;
import org.apache.commons.collections4.ListUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wiki.data.WikiDataParsedPage;
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

    public static void main(String[] args) throws IOException {
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
            throws ExecutionException, InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            final WikidataJsonParser wikidataJsonParser = new WikidataJsonParser();
            Callable<Map<String, WikiDataParsedPage>> wikiRun = () -> {
                Map<String, WikiDataParsedPage> parsePages = wikidataJsonParser.parse(inputDump, lang);
                WikiDataParsedPage.replaceIdsWithTitles(parsePages);
                return parsePages;
            };
            Future<Map<String, WikiDataParsedPage>> submitWikidata = executor.submit(wikiRun);

            Callable<Map<String, String>> wikipediaRun = () -> elasticAPI.readAllWikipediaIdsTitles(-1);
            Future<Map<String, String>> submitElastic = executor.submit(wikipediaRun);

            Map<String, String> wikipediaTitleToId = submitElastic.get();
            Map<String, WikiDataParsedPage> parsedPagesWikidata = submitWikidata.get();

            return WikiDataParsedPage.updateElasticIds(parsedPagesWikidata, wikipediaTitleToId);
        } finally {
            SimpleExecutorService.shutDownPool(executor);
        }
    }

    private static void updateAll(ElasticAPI elasticAPI, List<WikiDataParsedPage> wikiDataParsedPages, int bulkSize) throws IOException {
        if(wikiDataParsedPages != null) {
            ListUtils.partition(wikiDataParsedPages, bulkSize).forEach(elasticAPI::updateBulkWikidataAsnc);
        }
    }
}
