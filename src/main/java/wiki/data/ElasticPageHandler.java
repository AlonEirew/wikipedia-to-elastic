/**
 * @author  Alon Eirew
 */

package wiki.data;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkResponse;
import wiki.elastic.IElasticAPI;

import java.util.ArrayList;
import java.util.List;

public class ElasticPageHandler implements IPageHandler {

    private static final int BULK_SIZE = 1000;

    private final IElasticAPI elasticApi;
    private final ActionListener<BulkResponse> listener;
    private final String indexName;
    private final String dataType;

    private final List<WikiParsedPage> pages = new ArrayList<>();

    public ElasticPageHandler(IElasticAPI elasticApi, ActionListener<BulkResponse> listener, String indexName, String dataType) {
        this.elasticApi = elasticApi;
        this.listener = listener;
        this.indexName = indexName;
        this.dataType = dataType;
    }

    @Override
    public void addPage(WikiParsedPage page) {
        if(page != null) {
            pages.add(page);
            if(pages.size() == BULK_SIZE) {
                flush();
            }
        }
    }

    @Override
    public void flush() {
        if(this.pages != null && this.pages.size() > 0) {
            List<WikiParsedPage> copyPages = getPagesCopy();
            this.pages.clear();
            elasticApi.addBulkAsnc(this.listener, this.indexName, this.dataType, copyPages);
        }
    }

    private List<WikiParsedPage> getPagesCopy() {
        return new ArrayList<>(this.pages);
    }
}
