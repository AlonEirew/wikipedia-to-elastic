/**
 * @author  Alon Eirew
 */

package wiki.elastic;

import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.ElasticsearchStatusException;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.admin.indices.open.OpenIndexRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import wiki.data.WikiParsedPage;
import wiki.utils.WikiToElasticConfiguration;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class ElasticAPI implements Closeable {

    private final static Logger LOGGER = LogManager.getLogger(ElasticAPI.class);
    private static final Gson GSON = new Gson();
    private final static int MAX_AVAILABLE = 10;

    private final AtomicInteger totalIdsProcessed = new AtomicInteger(0);
    private final AtomicInteger totalIdsSuccessfullyCommitted = new AtomicInteger(0);

    // Limit the number of threads accessing elastic in parallel
    private final Semaphore available = new Semaphore(MAX_AVAILABLE, true);
    private final RestHighLevelClient client;
    private final Object closeLock = new Object();

    public ElasticAPI(RestHighLevelClient client) {
        this.client = client;
    }

    public ElasticAPI(WikiToElasticConfiguration configuration) {
        File wikifile = new File(configuration.getWikiDump());
        if (wikifile.exists()) {
            // init elastic client
            this.client = new RestHighLevelClient(
                    RestClient.builder(
                            new HttpHost(configuration.getHost(),
                                    configuration.getPort(),
                                    configuration.getScheme())));
        } else {
            this.client = null;
            throw new IllegalArgumentException("Dump not found at-" + configuration.getWikiDump());
        }
    }

    public DeleteIndexResponse deleteIndex(String indexName) throws ConnectException {
        DeleteIndexResponse deleteIndexResponse = null;
        try {
            DeleteIndexRequest delRequest = new DeleteIndexRequest(indexName);
            this.available.acquire();
            deleteIndexResponse = this.client.indices().delete(delRequest);
            this.available.release();
            LOGGER.info("Index " + indexName + " deleted successfully: " + deleteIndexResponse.isAcknowledged());
        } catch (ElasticsearchException ese) {
            if (ese.status() == RestStatus.NOT_FOUND) {
                LOGGER.info("Index " + indexName + " not found");
            } else {
                LOGGER.debug(ese);
            }
        } catch (ConnectException e) {
            LOGGER.error("Could not connect to elasticsearch...");
            throw e;
        } catch (IOException | InterruptedException e) {
            LOGGER.debug(e);
        }

        return deleteIndexResponse;
    }

    public CreateIndexResponse createIndex(WikiToElasticConfiguration configuration) throws IOException {
        CreateIndexResponse createIndexResponse = null;
        try {
            // Create the index
            CreateIndexRequest crRequest = new CreateIndexRequest(configuration.getIndexName());

            // Create shards & replicas
            Settings.Builder builder = Settings.builder();
            builder
                    .put("index.number_of_shards", configuration.getShards())
                    .put("index.number_of_replicas", configuration.getReplicas());

            String settingFileContent = configuration.getSettingFileContent();
            if(settingFileContent != null && !settingFileContent.isEmpty()) {
                builder.loadFromSource(settingFileContent, XContentType.JSON);
            }
            crRequest.settings(builder);

            // Create index mapping
            String mappingFileContent = configuration.getMappingFileContent();
            if(mappingFileContent != null && !mappingFileContent.isEmpty()) {
                crRequest.mapping(configuration.getDocType(), mappingFileContent, XContentType.JSON);
            }

            this.available.acquire();
            createIndexResponse = this.client.indices().create(crRequest);
            this.available.release();

            LOGGER.info("Index " + configuration.getIndexName() + " created successfully: " + createIndexResponse.isAcknowledged());
        } catch (InterruptedException e) {
            LOGGER.error("Could not creat elasticsearch index");
        }

        return createIndexResponse;
    }

    public synchronized void onSuccess(int successCount) {
        this.available.release();
        this.totalIdsSuccessfullyCommitted.addAndGet(successCount);
        this.totalIdsProcessed.addAndGet(-successCount);
        synchronized (closeLock) {
            closeLock.notify();
        }
    }

    public synchronized void onFail(int failedCount) {
        this.available.release();
        this.totalIdsProcessed.addAndGet(-failedCount);
        synchronized (closeLock) {
            closeLock.notify();
        }
    }

    public void addDocAsnc(String indexName, String indexType, WikiParsedPage page) {
        if(isValidRequest(indexName, indexType, page)) {
            IndexRequest indexRequest = createIndexRequest(
                    indexName,
                    indexType,
                    page);

            try {
                this.available.acquire();
                ElasticDocCreateListener listener = new ElasticDocCreateListener(indexRequest, this);
                this.client.indexAsync(indexRequest, listener);
                this.totalIdsProcessed.incrementAndGet();
                LOGGER.trace("Doc with Id " + page.getId() + " will be created asynchronously");
            } catch (InterruptedException e) {
                LOGGER.debug(e);
            }
        }
    }

    public void retryAddDoc(IndexRequest indexRequest, ElasticDocCreateListener listener) {
        try {
            // Release to give chance for other threads that waiting to execute
            this.available.release();
            this.available.acquire();
            this.client.indexAsync(indexRequest, listener);
            LOGGER.trace("Doc with Id " + indexRequest.id() + " will retry asynchronously");
        } catch (InterruptedException e) {
            LOGGER.debug(e);
        }
    }

    public IndexResponse addDoc(String indexName, String indexType, WikiParsedPage page) {
        IndexResponse res = null;

        try {
            if(isValidRequest(indexName, indexType, page)) {
                IndexRequest indexRequest = createIndexRequest(
                        indexName,
                        indexType,
                        page);

                this.available.acquire();
                res = this.client.index(indexRequest);
                this.available.release();
                this.totalIdsSuccessfullyCommitted.incrementAndGet();
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

        return res;
    }

    public void addBulkAsnc(String indexName, String indexType, List<WikiParsedPage> pages) {
        BulkRequest bulkRequest = new BulkRequest();

        if(pages != null) {
            for (WikiParsedPage page : pages) {
                if (isValidRequest(indexName, indexType, page)) {
                    IndexRequest request = createIndexRequest(indexName, indexType, page);
                    bulkRequest.add(request);
                }
            }


            try {
                // release will happen from listener (async)
                this.available.acquire();
                ElasticBulkDocCreateListener listener = new ElasticBulkDocCreateListener(bulkRequest, this);
                this.client.bulkAsync(bulkRequest, listener);
                this.totalIdsProcessed.addAndGet(pages.size());
                LOGGER.debug("Bulk insert will be created asynchronously");
            } catch (InterruptedException e) {
                LOGGER.error("Failed to acquire semaphore, lost bulk insert!", e);
            }
        }
    }

    public void retryAddBulk(BulkRequest bulkRequest, ElasticBulkDocCreateListener listener) {
        try {
            // Release to give chance for other threads that waiting to execute
            this.available.release();
            this.available.acquire();
            this.client.bulkAsync(bulkRequest, listener);
            LOGGER.debug("Bulk insert retry");
        } catch (InterruptedException e) {
            LOGGER.error("Failed to acquire semaphore, lost bulk insert!", e);
        }
    }

    public boolean isDocExists(String indexName, String indexType, String docId) {
        GetRequest getRequest = new GetRequest(
                indexName,
                indexType,
                docId);

        try {
            this.available.acquire();
            GetResponse getResponse = this.client.get(getRequest);
            this.available.release();
            if (getResponse.isExists()) {
                return true;
            }
        } catch (ElasticsearchStatusException | IOException | InterruptedException e) {
            LOGGER.error(e);
        }

        return false;
    }

    public boolean isIndexExists(String indexName) {
        boolean ret = false;
        try {
            OpenIndexRequest openIndexRequest = new OpenIndexRequest(indexName);
            ret = client.indices().open(openIndexRequest).isAcknowledged();
        } catch (ElasticsearchStatusException | IOException ignored) {
        }

        return ret;
    }

    public int getTotalIdsProcessed() {
        return totalIdsProcessed.get();
    }

    public int getTotalIdsSuccessfullyCommitted() {
        return totalIdsSuccessfullyCommitted.get();
    }

    private IndexRequest createIndexRequest(String indexName, String indexType, WikiParsedPage page) {
        IndexRequest indexRequest = new IndexRequest(
                indexName,
                indexType,
                String.valueOf(page.getId()));

        indexRequest.source(GSON.toJson(page), XContentType.JSON);

        return indexRequest;
    }

    private boolean isValidRequest(String indexName, String indexType, WikiParsedPage page) {
        return page != null && page.getId() > 0 && page.getTitle() != null && !page.getTitle().isEmpty() &&
                indexName != null && !indexName.isEmpty() && indexType != null && !indexType.isEmpty();
    }

    @Override
    public void close() throws IOException {
        if(client != null) {
            LOGGER.info("Closing RestHighLevelClient..");
            try {
                synchronized(closeLock) {
                    while(this.totalIdsProcessed.get() != 0) {
                        LOGGER.info("Waiting for " + this.totalIdsProcessed.get() + " async requests to complete...");
                        closeLock.wait();
                    }
                    client.close();
                }
            } catch (InterruptedException e) {
                client.close();
            }
        }
    }
}
