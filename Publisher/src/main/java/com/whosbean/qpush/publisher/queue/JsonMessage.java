package com.whosbean.qpush.publisher.queue;

import com.whosbean.qpush.client.PayloadMessage;

import java.io.Serializable;

/**
 * Created by yaming_deng on 14-8-8.
 */
public class JsonMessage implements Serializable {

    private PayloadMessage body;

    public PayloadMessage getBody() {
        return body;
    }

    public void setBody(PayloadMessage body) {
        this.body = body;
    }
}
