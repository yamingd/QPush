package com.whosbean.qpush.publisher.queue;

import com.whosbean.qpush.core.entity.Payload;

import java.io.Serializable;

/**
 * Created by yaming_deng on 14-8-8.
 */
public class JsonMessage implements Serializable {

    private Payload body;

    public Payload getBody() {
        return body;
    }

    public void setBody(Payload body) {
        this.body = body;
    }
}
