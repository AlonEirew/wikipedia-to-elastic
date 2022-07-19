/**
 * @author  Alon Eirew
 */

package wiki.persistency;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteRequest;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.bulk.BulkItemResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;

import java.util.concurrent.atomic.AtomicInteger;

public class ElasticBulkDocCreateListener implements ActionListener<BulkResponse> {

    private final static Logger LOGGER = LogManager.getLogger(ElasticBulkDocCreateListener.class);
    private final static int MAX_RETRY = 3;

    private final AtomicInteger count =  new AtomicInteger();
    private final ElasticAPI elasicApi;
    private final BulkRequest bulkRequest;

    public ElasticBulkDocCreateListener(BulkRequest bulkRequest, ElasticAPI elasicApi) {
        this.bulkRequest = bulkRequest;
        this.elasicApi = elasicApi;
    }

    @Override
    public void onResponse(BulkResponse bulkResponse) {
        this.elasicApi.onSuccess(bulkResponse.getItems().length);
        int noops = 0;
        StringBuilder sb = new StringBuilder();
        sb.append("Bulk Created/Updated done successfully, ids: [");
        for (BulkItemResponse bulkItemResponse : bulkResponse) {
            DocWriteResponse itemResponse = bulkItemResponse.getResponse();

            if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                    || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE ||
                    bulkItemResponse.getOpType() == DocWriteRequest.OpType.UPDATE) {
                String id = itemResponse.getId();
                if (itemResponse.getResult() == DocWriteResponse.Result.CREATED) {
                    sb.append(id).append(";");
                } else if (itemResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                    sb.append(id).append(";");
                } else if(itemResponse.getResult() == DocWriteResponse.Result.NOOP) {
                    noops ++;
                }
            }
        }
        sb.append("]");

        this.elasicApi.updateNOOPs(noops);
        LOGGER.debug(sb.toString());
    }

    @Override
    public void onFailure(Exception e) {
        LOGGER.error("Failed to commit some pages with exception=" + e.getMessage());
        if (count.incrementAndGet() < MAX_RETRY) {
            this.elasicApi.retryAddBulk(this.bulkRequest, this);
        } else {
            this.elasicApi.onFail(bulkRequest.requests().size());
            LOGGER.error("Failed, max retry exceeded, throwing request!");
        }
    }
}
