package com.whosbean.qpush.publisher.queue;

import java.io.Serializable;

/**
 * Created by yaming_deng on 14-8-8.
 */
public class JsonMessage implements Serializable {

    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}
