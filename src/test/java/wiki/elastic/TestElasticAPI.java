package wiki.elastic;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import wiki.data.WikiDataParsedPage;
import wiki.data.WikipediaParsedPage;
import wiki.data.WikipediaParsedPageBuilder;
import wiki.utils.WikiToElasticConfiguration;
import wiki.utils.WikiToElasticUtils;
import wiki.utils.WikidataJsonParser;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TestElasticAPI {
    private static final Gson GSON = new Gson();

    private static WikiToElasticConfiguration configuration;
    private static RestHighLevelClient client;
    private static ElasticAPI elasicApi;


    private static void prepareTests() throws IOException {
        URL url = TestElasticAPI.class.getClassLoader().getResource("test_config.json");
        if(configuration == null && url != null) {
            String file = url.getFile();
            JsonReader reader = new JsonReader(new FileReader(file));
            configuration = GSON.fromJson(reader, WikiToElasticConfiguration.CONFIGURATION_TYPE);
        }

        assert configuration != null;

        // init elastic client
        if(elasicApi == null) {
            elasicApi = new ElasticAPI(configuration);
        }

        // Delete if index already exists
//        elasicApi.deleteIndex();

        // Create the index
//        elasicApi.createIndex(configuration);
    }

    public void testPutDocOnElastic() throws IOException, InterruptedException {
        // Create/Add Page
        // Create page
        List<WikipediaParsedPage> testPages = createTestPages();
        for (WikipediaParsedPage page : testPages) {
            elasicApi.addDocAsnc(page);
        }

        // Need to wait for index to be searchable
        Thread.sleep(2000);
        searchCreatedIndex(configuration, client, elasicApi);
    }

    public void testPutBulkOnElastic() throws IOException, InterruptedException {
        // Create/Add Page
        // Create page
        List<WikipediaParsedPage> testPages = createTestPages();

        elasicApi.addBulkAsnc(testPages);

        Thread.sleep(2000);

        searchCreatedIndex(configuration, client, elasicApi);
    }

    public void testIsDocExist() throws InterruptedException {
        // Create page
        List<WikipediaParsedPage> testPages = createTestPages();
        elasicApi.addBulkAsnc(testPages);

        Thread.sleep(2000);

        for(WikipediaParsedPage page : testPages) {
            assertTrue(elasicApi.isDocExists(String.valueOf(page.getId())));
        }

        assertFalse(elasicApi.isDocExists("1234"));
    }

    // Test Update
    public static void updateWikidataTest(String[] args) throws IOException {
        prepareTests();
        List<String> aliases = new ArrayList<>();
        aliases.add("Gas bubbler");
        aliases.add("Vla Cla");
        WikiDataParsedPage wikiDataParsedPage = new WikiDataParsedPage("Q1", "Derrick Martin",
                aliases, new ArrayList<>(), new ArrayList<>(), new ArrayList<>(),
                new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        wikiDataParsedPage.setElasticPageId("7701435");

        List<WikiDataParsedPage> parsedPages = new ArrayList<>();
        parsedPages.add(wikiDataParsedPage);
        elasicApi.updateBulkWikidataAsnc(parsedPages);
        elasicApi.close();
    }

    public static void main(String[] args) throws IOException {
        prepareTests();
        Map<String, String> elasticIdTitle = elasicApi.readAllWikipediaIdsTitles(-1);
        ArrayList<String> titles = new ArrayList<>(elasticIdTitle.keySet());
        Random rand = new Random(elasticIdTitle.size());
        assert elasticIdTitle.size() > 10000;
        File inputDump = new File("dumps/small_wikidata.json.bz2");
        WikidataJsonParser wikidataJsonParser = new WikidataJsonParser();
        Map<String, WikiDataParsedPage> parsePages = wikidataJsonParser.parse(inputDump, configuration.getLang());
        for(WikiDataParsedPage page : parsePages.values()) {
            page.setElasticPageId(titles.get(rand.nextInt(titles.size())));
            changeList(page.getAliases(), rand, titles);
            changeList(page.getHasCause(), rand, titles);
            changeList(page.getHasEffect(), rand, titles);
            changeList(page.getHasImmediateCause(), rand, titles);
            changeList(page.getHasPart(), rand, titles);
            changeList(page.getPartOf(), rand, titles);
        }
    }

    private static void changeList(List<String> list, Random rand, List<String> titles) {
        if(list != null) {
            for(int i = 0 ; i < list.size() ; i++) {
                list.set(i, titles.get(rand.nextInt(titles.size())));
            }
        }
    }

    private List<WikipediaParsedPage> createTestPages() {
        List<WikipediaParsedPage> wikiPages = new ArrayList<>();

        wikiPages.add(new WikipediaParsedPageBuilder()
                .setTitle("Test Doc")
                .setText("Testing elastic search")
                .setId(555)
                .setRedirectTitle("Redirect Test Doc Title")
                .build());

        wikiPages.add(new WikipediaParsedPageBuilder()
                .setTitle("Test")
                .setText("Testing elastic search")
                .setId(556)
                .setRedirectTitle("Redirect Test Doc Title")
                .build());

        wikiPages.add(new WikipediaParsedPageBuilder()
                .setTitle("Te st")
                .setText("Testing elastic search")
                .setId(557)
                .setRedirectTitle("Redirect Test Doc Title")
                .build());

        return wikiPages;
    }

    private void searchCreatedIndex(WikiToElasticConfiguration configuration, RestHighLevelClient client, ElasticAPI elasicApi) throws IOException {
        // Search page
        SearchRequest searchRequest = new SearchRequest(configuration.getIndexName());
        searchRequest.types(configuration.getDocType());

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("title", "Test"));
        sourceBuilder.size(5);
        sourceBuilder.timeout(new TimeValue(5, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);
        assertNotNull(searchResponse);
        System.out.println(searchResponse.toString());

        elasicApi.deleteIndex();
    }
}
