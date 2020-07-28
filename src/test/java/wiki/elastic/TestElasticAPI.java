package wiki.elastic;

import com.google.gson.stream.JsonReader;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
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

public class TestElasticAPI {

    private WikiToElasticConfiguration configuration;
    private RestHighLevelClient client;
    private ElasticAPI elasicApi;


    @Before
    public void prepareText() throws FileNotFoundException {
        URL url = TestElasticAPI.class.getClassLoader().getResource("test_conf.json");
        if(this.configuration == null && url != null) {
            String file = url.getFile();
            JsonReader reader = new JsonReader(new FileReader(file));
            this.configuration = WikiToElasticConfiguration.GSON.fromJson(reader, WikiToElasticConfiguration.CONFIGURATION_TYPE);
        }

        // init elastic client
        if(this.elasicApi == null) {
            this.client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(configuration.getHost(), configuration.getPort(), configuration.getScheme())));

            this.elasicApi = new ElasticAPI(client);
        }


        // Delete if index already exists
        this.elasicApi.deleteIndex(configuration.getIndexName());

        // Create the index
        this.elasicApi.createIndex(configuration);
    }

    @Test
    public void testPutDocOnElastic() throws IOException, InterruptedException {
        // Create/Add Page
        // Listener
        ActionListener<IndexResponse> listener = new ElasticDocCreateListener(this.elasicApi);

        // Create page
        List<WikiParsedPage> testPages = createTestPages();
        for (WikiParsedPage page : testPages) {
            this.elasicApi.addDocAsnc(listener, this.configuration.getIndexName(), this.configuration.getDocType(), page);
        }

        // Need to wait for index to be searchable
        Thread.sleep(2000);
        searchCreatedIndex(this.configuration, this.client, this.elasicApi);
    }

    @Test
    public void testPutBulkOnElastic() throws IOException, InterruptedException {
        // Create/Add Page
        // Listener
        ActionListener<BulkResponse> listener = new ElasticBulkDocCreateListener(this.elasicApi);

        // Create page
        List<WikiParsedPage> testPages = createTestPages();

        this.elasicApi.addBulkAsnc(listener, this.configuration.getIndexName(), this.configuration.getDocType(), testPages);

        Thread.sleep(2000);

        searchCreatedIndex(this.configuration, this.client, this.elasicApi);
    }

    @Test
    public void testIsDocExist() throws InterruptedException {
        // Create page
        List<WikiParsedPage> testPages = createTestPages();
        ActionListener<BulkResponse> listener = new ElasticBulkDocCreateListener(this.elasicApi);
        this.elasicApi.addBulkAsnc(listener, this.configuration.getIndexName(), this.configuration.getDocType(), testPages);

        Thread.sleep(2000);

        for(WikiParsedPage page : testPages) {
            Assert.assertTrue(this.elasicApi.isDocExists(this.configuration.getIndexName(),
                    this.configuration.getDocType(), String.valueOf(page.getId())));
        }

        Assert.assertFalse(this.elasicApi.isDocExists(this.configuration.getIndexName(),
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
        Assert.assertNotNull(searchResponse);
        System.out.println(searchResponse.toString());

        elasicApi.deleteIndex(configuration.getIndexName());
    }
}
