/**
 * @author  Alon Eirew
 */

package wiki.handlers;

import wiki.data.WikipediaParsedPage;
import wiki.persistency.ElasticAPI;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ElasticPageHandler implements IPageHandler {

    private final ElasticAPI elasticApi;
    private final int bulkSize;

    private final Object lock = new Object();

    private final List<WikipediaParsedPage> pages = new ArrayList<>();

    public ElasticPageHandler(ElasticAPI elasticApi, int bulkSize) {
        this.elasticApi = elasticApi;
        this.bulkSize = bulkSize;
    }

    @Override
    public boolean isPageExists(String pageId) {
        if(pageId != null && !pageId.isEmpty()) {
            return this.elasticApi.isDocExists(pageId);
        }

        return false;
    }

    /**
     * Add page to the handler queue, once queue is full (configuration in conf.json) the queue is persisted to elastic
     * and cleared
     * @param page
     */
    @Override
    public void addPage(WikipediaParsedPage page) {
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
                List<WikipediaParsedPage> copyPages = new ArrayList<>(this.pages);
                this.pages.clear();
                elasticApi.addBulkAsnc(copyPages);
            }
        }
    }

    public int getPagesQueueSize() {
        return this.pages.size();
    }

    @Override
    public void close() throws IOException {
        List<WikipediaParsedPage> copyPages = new ArrayList<>(this.pages);
        this.pages.clear();
        for(WikipediaParsedPage page : copyPages) {
            elasticApi.addDoc(page);
        }
    }
}
