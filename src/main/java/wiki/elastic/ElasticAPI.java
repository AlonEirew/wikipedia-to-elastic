/**
 * @author  Alon Eirew
 */

package wiki.elastic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.ElasticsearchException;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexRequest;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.rest.RestStatus;
import wiki.data.WikiParsedPage;
import wiki.utils.WikiToElasticConfiguration;

import java.io.IOException;
import java.util.List;

public class ElasticAPI implements IElasticAPI {

    private final static Logger LOGGER = LogManager.getLogger(ElasticAPI.class);

    private final RestHighLevelClient client;

    public ElasticAPI(RestHighLevelClient client) {
        this.client = client;
    }

    @Override
    public DeleteIndexResponse deleteIndex(String indexName) {
        DeleteIndexResponse deleteIndexResponse = null;
        try {
            DeleteIndexRequest delRequest = new DeleteIndexRequest(indexName);
            deleteIndexResponse = this.client.indices().delete(delRequest);
            LOGGER.info("Index " + indexName + " deleted successfully: " + deleteIndexResponse.isAcknowledged());
        } catch (ElasticsearchException ese) {
            if (ese.status() == RestStatus.NOT_FOUND) {
                LOGGER.info("Index " + indexName + " not found");
            } else {
                LOGGER.debug(ese);
            }
        } catch (IOException e) {
            LOGGER.debug(e);
        }

        return deleteIndexResponse;
    }

    @Override
    public CreateIndexResponse createIndex(WikiToElasticConfiguration configuration) {
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
            createIndexResponse = this.client.indices().create(crRequest);

            LOGGER.info("Index " + configuration.getIndexName() + " created successfully: " + createIndexResponse.isAcknowledged());
        } catch(IOException ex) {
            LOGGER.error(ex);
        }

        return createIndexResponse;
    }

    @Override
    public void addDocAsnc(ActionListener<IndexResponse> listener, String indexName, String indexType, WikiParsedPage page) {
        if(isValidRequest(indexName, indexType, page)) {
            IndexRequest indexRequest = createIndexRequest(
                    indexName,
                    indexType,
                    page);

            this.client.indexAsync(indexRequest, listener);
            LOGGER.trace("Doc with Id " + page.getId() + " will be created asynchronously");
        }
    }

    @Override
    public IndexResponse addDoc(String indexName, String indexType, WikiParsedPage page) {
        IndexResponse res = null;

        try {
            if(isValidRequest(indexName, indexType, page)) {
                IndexRequest indexRequest = createIndexRequest(
                        indexName,
                        indexType,
                        page);


                res = this.client.index(indexRequest);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return res;
    }

    @Override
    public void addBulkAsnc(ActionListener<BulkResponse> listener, String indexName, String indexType, List<WikiParsedPage> pages) {
        BulkRequest bulkRequest = new BulkRequest();

        if(pages != null) {
            for(WikiParsedPage page : pages) {
                if(isValidRequest(indexName, indexType, page)) {
                    IndexRequest request = createIndexRequest(indexName, indexType, page);
                    bulkRequest.add(request);
                }
            }
        }


        this.client.bulkAsync(bulkRequest, listener);
        LOGGER.debug("Bulk insert will be created asynchronously");
    }

    private IndexRequest createIndexRequest(String indexName, String indexType, WikiParsedPage page) {
        IndexRequest indexRequest = new IndexRequest(
                indexName,
                indexType,
                String.valueOf(page.getId()));

        indexRequest.source(WikiToElasticConfiguration.gson.toJson(page), XContentType.JSON);

        return indexRequest;
    }

    private boolean isValidRequest(String indexName, String indexType, WikiParsedPage page) {
        return page != null && page.getId() > 0 && page.getTitle() != null && !page.getTitle().isEmpty() &&
                indexName != null && !indexName.isEmpty() && indexType != null && !indexType.isEmpty();
    }
}
