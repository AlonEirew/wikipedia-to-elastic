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
import org.junit.Test;
import wiki.data.WikiParsedPage;
import wiki.data.WikiParsedPageBuilder;
import wiki.utils.WikiToElasticConfiguration;
import wiki.utils.TestWikiToElasticUtils;

import java.io.FileReader;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class TestElasticAPI {

    @Test
    public void testPutDocOnElastic() throws IOException, InterruptedException {
        TestWikiToElasticUtils tu = new TestWikiToElasticUtils();
        URL url = tu.getClass().getClassLoader().getResource("test_conf.json");
        if(url != null) {
            String file = url.getFile();
            JsonReader reader = new JsonReader(new FileReader(file));
            WikiToElasticConfiguration configuration = WikiToElasticConfiguration.gson.fromJson(reader, WikiToElasticConfiguration.CONFIGURATION_TYPE);

            // init elastic client
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(configuration.getHost(), configuration.getPort(), configuration.getScheme())));

            IElasticAPI elasicApi = new ElasticAPI(client);

            // Delete if index already exists
            elasicApi.deleteIndex(configuration.getIndexName());

            // Create the index
            elasicApi.createIndex(configuration);

            // Create/Add Page
            // Listener
            ActionListener<IndexResponse> listener = new ElasticDocCreateListener();

            // Create page
            List<WikiParsedPage> testPages = createTestPages();
            for (WikiParsedPage page : testPages) {
                elasicApi.addDocAsnc(listener, configuration.getIndexName(), configuration.getDocType(), page);
            }

            // Need to wait for index to be searchable
            Thread.sleep(2000);
            searchCreatedIndex(configuration, client, elasicApi);
        }
    }

    @Test
    public void testPutBulkOnElastic() throws IOException, InterruptedException {
        TestWikiToElasticUtils tu = new TestWikiToElasticUtils();
        URL url = tu.getClass().getClassLoader().getResource("test_conf.json");
        if(url != null) {
            String file = url.getFile();
            JsonReader reader = new JsonReader(new FileReader(file));
            WikiToElasticConfiguration configuration = WikiToElasticConfiguration.gson.fromJson(reader, WikiToElasticConfiguration.CONFIGURATION_TYPE);

            // init elastic client
            RestHighLevelClient client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(configuration.getHost(), configuration.getPort(), configuration.getScheme())));

            IElasticAPI elasicApi = new ElasticAPI(client);

            // Delete if index already exists
            elasicApi.deleteIndex(configuration.getIndexName());

            // Create the index
            elasicApi.createIndex(configuration);

            // Create/Add Page
            // Listener
            ActionListener<BulkResponse> listener = new ElasticBulkDocCreateListener();

            // Create page
            List<WikiParsedPage> testPages = createTestPages();

            elasicApi.addBulkAsnc(listener, configuration.getIndexName(), configuration.getDocType(), testPages);

            Thread.sleep(2000);

            searchCreatedIndex(configuration, client, elasicApi);
        }
    }

    private List<WikiParsedPage> createTestPages() {
        List<WikiParsedPage> wikiPages = new ArrayList<>();

        wikiPages.add(new WikiParsedPageBuilder()
                .setTitle("Test Doc")
                .setText("Testing elastic search")
                .setDescription(new HashSet<>())
                .setId(555)
                .setRedirectTitle("Redirect Test Doc Title")
                .setAliases(new HashSet<>())
                .build());

        wikiPages.add(new WikiParsedPageBuilder()
                .setTitle("Test")
                .setText("Testing elastic search")
                .setDescription(new HashSet<>())
                .setId(556)
                .setRedirectTitle("Redirect Test Doc Title")
                .setAliases(new HashSet<>())
                .build());

        wikiPages.add(new WikiParsedPageBuilder()
                .setTitle("Te st")
                .setText("Testing elastic search")
                .setDescription(new HashSet<>())
                .setId(557)
                .setRedirectTitle("Redirect Test Doc Title")
                .setAliases(new HashSet<>())
                .build());

        return wikiPages;
    }

    private void searchCreatedIndex(WikiToElasticConfiguration configuration, RestHighLevelClient client, IElasticAPI elasicApi) throws IOException {
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
