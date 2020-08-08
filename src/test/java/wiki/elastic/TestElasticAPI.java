package wiki.elastic;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import wiki.data.WikiParsedPage;
import wiki.data.WikiParsedPageBuilder;
import wiki.utils.WikiToElasticConfiguration;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

public class TestElasticAPI {
    private static final Gson GSON = new Gson();

    private WikiToElasticConfiguration configuration;
    private RestHighLevelClient client;
    private ElasticAPI elasicApi;


    public void prepareText() throws FileNotFoundException {
        URL url = TestElasticAPI.class.getClassLoader().getResource("test_config.json");
        if(this.configuration == null && url != null) {
            String file = url.getFile();
            JsonReader reader = new JsonReader(new FileReader(file));
            this.configuration = GSON.fromJson(reader, WikiToElasticConfiguration.CONFIGURATION_TYPE);
        }

        assert this.configuration != null;

        // init elastic client
        if(this.elasicApi == null) {
            this.elasicApi = new ElasticAPI(configuration);
        }


        // Delete if index already exists
        this.elasicApi.deleteIndex(configuration.getIndexName());

        // Create the index
        this.elasicApi.createIndex(configuration);
    }

    public void testPutDocOnElastic() throws IOException, InterruptedException {
        // Create/Add Page
        // Create page
        List<WikiParsedPage> testPages = createTestPages();
        for (WikiParsedPage page : testPages) {
            this.elasicApi.addDocAsnc(this.configuration.getIndexName(), this.configuration.getDocType(), page);
        }

        // Need to wait for index to be searchable
        Thread.sleep(2000);
        searchCreatedIndex(this.configuration, this.client, this.elasicApi);
    }

    public void testPutBulkOnElastic() throws IOException, InterruptedException {
        // Create/Add Page
        // Create page
        List<WikiParsedPage> testPages = createTestPages();

        this.elasicApi.addBulkAsnc(this.configuration.getIndexName(), this.configuration.getDocType(), testPages);

        Thread.sleep(2000);

        searchCreatedIndex(this.configuration, this.client, this.elasicApi);
    }

    public void testIsDocExist() throws InterruptedException {
        // Create page
        List<WikiParsedPage> testPages = createTestPages();
        this.elasicApi.addBulkAsnc(this.configuration.getIndexName(), this.configuration.getDocType(), testPages);

        Thread.sleep(2000);

        for(WikiParsedPage page : testPages) {
            assertTrue(this.elasicApi.isDocExists(this.configuration.getIndexName(),
                    this.configuration.getDocType(), String.valueOf(page.getId())));
        }

        assertFalse(this.elasicApi.isDocExists(this.configuration.getIndexName(),
                this.configuration.getDocType(), "1234"));
    }

    private List<WikiParsedPage> createTestPages() {
        List<WikiParsedPage> wikiPages = new ArrayList<>();

        wikiPages.add(new WikiParsedPageBuilder()
                .setTitle("Test Doc")
                .setText("Testing elastic search")
                .setId(555)
                .setRedirectTitle("Redirect Test Doc Title")
                .build());

        wikiPages.add(new WikiParsedPageBuilder()
                .setTitle("Test")
                .setText("Testing elastic search")
                .setId(556)
                .setRedirectTitle("Redirect Test Doc Title")
                .build());

        wikiPages.add(new WikiParsedPageBuilder()
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

        elasicApi.deleteIndex(configuration.getIndexName());
    }
}
