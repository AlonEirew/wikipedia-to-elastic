package wiki;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.search.Scroll;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import wiki.data.WikiDataParsedPage;
import wiki.elastic.ElasticAPI;
import wiki.utils.WikiToElasticConfiguration;
import wiki.utils.WikiToElasticUtils;
import wiki.utils.WikidataJsonParser;

import java.io.*;
import java.util.Map;
import java.util.Scanner;

import static org.elasticsearch.index.query.QueryBuilders.matchAllQuery;

public class WikiDataEnrich {
    private final static Logger LOGGER = LogManager.getLogger(WikiDataEnrich.class);
    private final static Gson GSON = new Gson();


    public static void main(String[] args) throws FileNotFoundException {
        File inputDump = new File("dumps/small_wikidata.json.bz2");
        WikiToElasticConfiguration config = GSON.fromJson(new FileReader("conf.json"), WikiToElasticConfiguration.class);
        WikidataJsonParser wikidataJsonParser = new WikidataJsonParser();

        try(InputStream inputStream = WikiToElasticUtils.openCompressedFileInputStream(inputDump.getPath());
                Scanner reader = new Scanner(System.in); ElasticAPI elasticAPI = new ElasticAPI(config)) {
            System.out.println("Press enter to continue...");
            reader.nextLine();
            long startTime = System.currentTimeMillis();
            Map<String, WikiDataParsedPage> parsePages = wikidataJsonParser.parse(inputStream);
            WikiDataParsedPage.replaceIdsWithTitles(parsePages);
            Map<String, String> wikipeida = elasticAPI.readAllWikipedia(-1);
            long endTime = System.currentTimeMillis();
            System.out.println("Done, took-" + (endTime - startTime) + "ms");
        } catch (IOException e) {
            LOGGER.error("Something went wrong!", e);
        }
    }

    private static void getAllTitles() throws IOException {
        final Scroll scroll = new Scroll(TimeValue.timeValueHours(5L));
//        SearchResponse searchResponse = createElasticSearchResponse(elasticClient, scroll);
    }

    private static SearchResponse createElasticSearchResponse(RestHighLevelClient elasticClient, Scroll scroll) throws IOException {
        final SearchRequest searchRequest = new SearchRequest("enwiki_v4");
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(matchAllQuery());
        searchSourceBuilder.size(1000);
        searchRequest.source(searchSourceBuilder);
        searchRequest.scroll(scroll);
        return elasticClient.search(searchRequest);
    }
}
