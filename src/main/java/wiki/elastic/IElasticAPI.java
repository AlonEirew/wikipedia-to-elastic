/**
 * @author  Alon Eirew
 */

package wiki.elastic;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.create.CreateIndexResponse;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;
import wiki.data.WikiParsedPage;
import wiki.utils.WikiToElasticConfiguration;

import java.util.List;

public interface IElasticAPI {
    DeleteIndexResponse deleteIndex(String indexName);
    CreateIndexResponse createIndex(WikiToElasticConfiguration configuration);
    void addDocAsnc(ActionListener<IndexResponse> listener, String indexName, String indexType, WikiParsedPage page);
    void addBulkAsnc(ActionListener<BulkResponse> listener, String indexName, String indexType, List<WikiParsedPage> pages);
}
