package wiki.elastic;

import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class TestSearchOnLive {

    private RestHighLevelClient client;
    private ElasticAPI elasicApi;

    @Before
    public void prepareText() {
        // init elastic client
        if(this.elasicApi == null) {
            this.client = new RestHighLevelClient(
                    RestClient.builder(
                                new HttpHost("localhost", 9200, "http")));

            this.elasicApi = new ElasticAPI(client);
        }
    }

//    @Test
    public void testPutAndSearchDocOnElastic() throws InterruptedException, IOException {
        // Create/Add Page
        // Create page
        WikiParsedPage page = createRealPages();
        this.elasicApi.addDocAsnc("enwiki_v2", "wikipage", page);

        // Search page
        SearchRequest searchRequest = new SearchRequest("enwiki_v2");
        searchRequest.types("wikipage");

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("title.near_match", "land"));
        sourceBuilder.size(5);
        sourceBuilder.timeout(new TimeValue(5, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);
        Assert.assertNotNull(searchResponse);
        System.out.println(searchResponse.toString());
    }

    private WikiParsedPage createRealPages() {
        return new WikiParsedPageBuilder()
                .setTitle("Land")
                .setText("Testing elastic search")
                .setId(555)
                .setRedirectTitle("Redirect Test Doc Title")
                .build();
    }
}
