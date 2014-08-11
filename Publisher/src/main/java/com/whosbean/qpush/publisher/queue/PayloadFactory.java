package com.whosbean.qpush.publisher.queue;

import com.lmax.disruptor.EventFactory;

/**
 * Created by yaming_deng on 14-8-8.
 */
public class PayloadFactory implements EventFactory<JsonMessage> {

    @Override
    public JsonMessage newInstance() {
        return new JsonMessage();
    }
}
