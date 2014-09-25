package com.whosbean.qpush.publisher.queue;

import com.lmax.disruptor.EventHandler;
import com.whosbean.qpush.client.PayloadMessage;
import com.whosbean.qpush.publisher.handler.PayloadHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 队列消息处理.
 * Created by yaming_deng on 14-8-8.
 */
public class PayloadConsumer implements EventHandler<JsonMessage> {

    protected static Logger logger = LoggerFactory.getLogger(PayloadConsumer.class);

    public PayloadConsumer(){

    }

    @Override
    public void onEvent(JsonMessage event, long sequence, boolean endOfBatch) throws Exception {
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

        PayloadMessage message = event.getBody();
        PayloadHandler.instance.save(message);
    }

}
