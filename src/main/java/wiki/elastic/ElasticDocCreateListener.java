/**
 * @author  Alon Eirew
 */

package wiki.elastic;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.DocWriteResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;

import java.util.concurrent.atomic.AtomicInteger;

public class ElasticDocCreateListener implements ActionListener<IndexResponse> {

    private final static Logger LOGGER = LogManager.getLogger(ElasticDocCreateListener.class);
    private final static int MAX_RETRY = 3;

    private final AtomicInteger count =  new AtomicInteger();
    private final ElasticAPI elasicApi;
    private final IndexRequest indexRequest;

    public ElasticDocCreateListener(IndexRequest indexRequest, ElasticAPI elasicApi) {
        this.indexRequest = indexRequest;
        this.elasicApi = elasicApi;
    }

    @Override
    public void onResponse(IndexResponse indexResponse) {
        this.elasicApi.onSuccess(1);
        String index = indexResponse.getIndex();
        String id = indexResponse.getId();
        if (indexResponse.getResult() == DocWriteResponse.Result.CREATED) {
            LOGGER.trace("document with id:" + id + " Created successfully at index:" + index);
        } else if (indexResponse.getResult() == DocWriteResponse.Result.UPDATED) {
            LOGGER.trace("document with id:" + id + " Updated successfully at " + index);
        }
    }

    @Override
    public void onFailure(Exception e) {
        LOGGER.error("failed inserting document with exception=" + e.getMessage());
        if (count.incrementAndGet() < MAX_RETRY) {
            this.elasicApi.retryAddDoc(indexRequest, this);
        } else {
            this.elasicApi.onFail(1);
            LOGGER.error("Failed, max retry exceeded, throwing request!");
        }
    }
}
