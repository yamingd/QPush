package com.argo.qpush.client;


/**
 * Created by Yaming on 2014/10/27.
 */
public interface ChannelAvailable {

    /**
     *
     * @param ctx
     */
    void execute(final ClientConnection ctx);

    /**
     *
     */
    void error(Exception e);
}
