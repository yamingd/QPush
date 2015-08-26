package com.argo.qpush.client;


import io.netty.channel.ChannelHandlerContext;

/**
 * Created by Yaming on 2014/10/27.
 */
public interface ChannelAvailable {

    /**
     *
     * @param ctx
     */
    void execute(final ChannelHandlerContext ctx);

    /**
     *
     */
    void error(Exception e);
}
