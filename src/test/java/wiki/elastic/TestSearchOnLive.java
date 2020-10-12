package wiki.elastic;

import org.apache.http.HttpHost;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import wiki.data.WikipediaParsedPage;
import wiki.data.WikipediaParsedPageBuilder;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestSearchOnLive {

    private RestHighLevelClient client;
    private ElasticAPI elasicApi;

    public void testPutAndSearchDocOnElastic() throws InterruptedException, IOException {
        // Create/Add Page
        // Create page
        WikipediaParsedPage page = createRealPages();
        this.elasicApi.addDocAsnc(page);

        // Search page
        SearchRequest searchRequest = new SearchRequest("enwiki_v2");
        searchRequest.types("wikipage");

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery("title.near_match", "land"));
        sourceBuilder.size(5);
        sourceBuilder.timeout(new TimeValue(5, TimeUnit.SECONDS));
        searchRequest.source(sourceBuilder);

        SearchResponse searchResponse = client.search(searchRequest);
        assertNotNull(searchResponse);
        System.out.println(searchResponse.toString());
    }

    private WikipediaParsedPage createRealPages() {
        return new WikipediaParsedPageBuilder()
                .setTitle("Land")
                .setText("Testing elastic search")
                .setId(555)
                .setRedirectTitle("Redirect Test Doc Title")
                .build();
    }
}
