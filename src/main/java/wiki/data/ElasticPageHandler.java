/**
 * @author  Alon Eirew
 */

package wiki.data;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.bulk.BulkResponse;
import wiki.elastic.IElasticAPI;
import wiki.utils.WikiToElasticConfiguration;

import java.util.ArrayList;
import java.util.List;

public class ElasticPageHandler implements IPageHandler {

    private final IElasticAPI elasticApi;
    private final ActionListener<BulkResponse> listener;
    private final String indexName;
    private final String docType;
    private final int bulkSize;

    private final List<WikiParsedPage> pages = new ArrayList<>();

    public ElasticPageHandler(IElasticAPI elasticApi, ActionListener<BulkResponse> listener, WikiToElasticConfiguration config) {
        this.elasticApi = elasticApi;
        this.listener = listener;
        this.indexName = config.getIndexName();
        this.docType = config.getDocType();
        this.bulkSize = config.getInsertBulkSize();
    }

    @Override
    public void addPage(WikiParsedPage page) {
        if(page != null) {
            pages.add(page);
            if(pages.size() == this.bulkSize) {
                flush();
            }
        }
    }

    @Override
    public void flush() {
        if(this.pages != null && this.pages.size() > 0) {
            List<WikiParsedPage> copyPages = getPagesCopy();
            this.pages.clear();
            elasticApi.addBulkAsnc(this.listener, this.indexName, this.docType, copyPages);
        }
    }

    private List<WikiParsedPage> getPagesCopy() {
        return new ArrayList<>(this.pages);
    }
}
