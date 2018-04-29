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

    @Override
    public void onResponse(BulkResponse bulkResponse) {
        for (BulkItemResponse bulkItemResponse : bulkResponse) {
            DocWriteResponse itemResponse = bulkItemResponse.getResponse();

            if (bulkItemResponse.getOpType() == DocWriteRequest.OpType.INDEX
                    || bulkItemResponse.getOpType() == DocWriteRequest.OpType.CREATE) {
                IndexResponse indexResponse = (IndexResponse) itemResponse;
                String index = indexResponse.getIndex();
                String id = indexResponse.getId();
                if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
                    LOGGER.debug("document with id:" + id + " Created successfully at index:" + index);
                } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
                    LOGGER.debug("document with id:" + id + " Updated successfully at " + index);
                }
            }
        }
    }

    @Override
    public void onFailure(Exception e) {

    }
}
