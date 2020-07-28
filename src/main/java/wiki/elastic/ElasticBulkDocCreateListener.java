/**
 * @author  Alon Eirew
 */

package wiki.elastic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexResponse;

public class ElasticBulkDocCreateListener implements ActionListener<BulkResponse> {

    private final static Logger LOGGER = LogManager.getLogger(ElasticDocCreateListener.class);
    private final ElasticAPI elasicApi;

    public ElasticBulkDocCreateListener(ElasticAPI elasicApi) {
        this.elasicApi = elasicApi;
    }

    @Override
    public void onResponse(BulkResponse bulkResponse) {
        this.elasicApi.releaseSemaphore();
        StringBuilder sb = new StringBuilder();
        sb.append("Bulk Created/Updated done successfully, ids: [");
        for (BulkItemResponse bulkItemResponse : bulkResponse) {
            DocWriteResponse itemResponse = bulkItemResponse.getResponse();

            if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                    || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                IndexResponse indexResponse = (IndexResponse) itemResponse;
                String id = indexResponse.getId();
                if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                    sb.append(id).append(";");
                } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                    sb.append(id).append(";");
                }
            }
        }
        sb.append("]");
        LOGGER.debug(sb.toString());
    }

    @Override
    public void onFailure(Exception e) {
        this.elasicApi.releaseSemaphore();
        LOGGER.error("Failed to commit some pages", e);
    }
}
