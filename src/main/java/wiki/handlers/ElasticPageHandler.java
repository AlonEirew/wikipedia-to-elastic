/**
 * @author  Alon Eirew
 */

package wiki.handlers;

import wiki.data.WikiParsedPage;
import wiki.elastic.ElasticAPI;
import wiki.utils.WikiToElasticConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElasticPageHandler implements IPageHandler {

    private final ElasticAPI elasticApi;
    private final String indexName;
    private final String docType;
    private final int bulkSize;

    private final Object lock = new Object();

    private final List<WikiParsedPage> pages = new ArrayList<>();

    public ElasticPageHandler(ElasticAPI elasticApi, WikiToElasticConfiguration config) {
        this.elasticApi = elasticApi;
        this.indexName = config.getIndexName();
        this.docType = config.getDocType();
        this.bulkSize = config.getInsertBulkSize();
    }

    @Override
    public boolean isPageExists(String pageId) {
        if(pageId != null && !pageId.isEmpty()) {
            return this.elasticApi.isDocExists(this.indexName, this.docType, pageId);
        }

        return false;
    }

    /**
     * Add page to the handler queue, once queue is full (configuration in conf.json) the queue is persisted to elastic
     * and cleared
     * @param page
     */
    @Override
    public void addPage(WikiParsedPage page) {
        synchronized (this.lock) {
            if (page != null) {
                pages.add(page);
                if (this.pages.size() == this.bulkSize) {
                    flush();
                }
            }
        }
    }

    @Override
    public void flush() {
        synchronized (this.lock) {
            if (this.pages.size() > 0) {
                List<WikiParsedPage> copyPages = new ArrayList<>(this.pages);
                this.pages.clear();
                elasticApi.addBulkAsnc(this.indexName, this.docType, copyPages);
            }
        }
    }

    public int getPagesQueueSize() {
        return this.pages.size();
    }

    @Override
    public void close() throws IOException {
        List<WikiParsedPage> copyPages = new ArrayList<>(this.pages);
        this.pages.clear();
        for(WikiParsedPage page : copyPages) {
            elasticApi.addDoc(this.indexName, this.docType, page);
        }
    }
}
