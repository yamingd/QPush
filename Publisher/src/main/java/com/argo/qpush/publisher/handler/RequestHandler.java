package com.argo.qpush.publisher.handler;

import com.argo.qpush.client.RequestMessage;

/**
 * Created by yamingd on 8/7/15.
 */
public interface RequestHandler {

    /**
     *
     * @param request
     * @throws Exception
     */
    void handle(RequestMessage request) throws Exception;

}
