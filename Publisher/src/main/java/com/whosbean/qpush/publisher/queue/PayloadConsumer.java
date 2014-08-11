package com.whosbean.qpush.publisher.queue;

import com.lmax.disruptor.EventHandler;
import com.whosbean.qpush.core.GsonUtils;
import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.core.entity.Product;
import com.whosbean.qpush.core.service.ProductService;
import com.whosbean.qpush.pipe.PayloadQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * 队列消息处理.
 * Created by yaming_deng on 14-8-8.
 */
public class PayloadConsumer implements EventHandler<JsonMessage> {

    private static final String msg = "Handler[%s], totalEvent=%s, totalDuration=%s ms, avgDuration=%s ms, lastDuration=%s ms";
    protected static Logger logger = LoggerFactory.getLogger(PayloadConsumer.class);
    private int count = 0;
    private long totalDuration = 0;
    private PayloadQueue payloadQueue;

    public PayloadConsumer(PayloadQueue handler){
        this.payloadQueue = handler;
    }

    @Override
    public void onEvent(JsonMessage event, long sequence, boolean endOfBatch) throws Exception {
        long startTs = new Date().getTime();
        count ++;

        //0. 构造消息
        /**
         * {
         *  "title": "abc",
         *  "badge": 10,
         *  "sound": "x.acf",
         *  "appkey": "abcdef",
         *  "brocast": 0/1
         *  "clients" : [
         *      "ab0",
         *      "ab1"
         *  ]
         *  "ext": {
         *     "key1": "value1",
         *     "key2": "value2"
         *  }
         *  }
         */

        Payload payload = GsonUtils.asT(Payload.class, event.getBody());
        Product product = ProductService.instance.findByKey(payload.getAppkey());
        if (product != null) {
            payload.setProductId(product.getId());
            this.payloadQueue.add(payload);
        }else{
            logger.error("Product not found. appkey=" + payload.getAppkey());
        }

        long duration = new Date().getTime() - startTs;
        totalDuration += duration;

        long threadId = Thread.currentThread().getId();
        logger.info(String.format(msg, threadId, count, totalDuration, totalDuration / count, duration));
    }

}
