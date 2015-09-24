package com.argo.qpush.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yaming_deng on 14-8-11.
 */
public class ClientConnectHandler extends ChannelInboundHandlerAdapter {

    protected static Logger logger = LoggerFactory.getLogger(ClientConnectHandler.class);

    private ClientConnection clientConnection;

    public ClientConnectHandler(ClientConnection connection){
        this.clientConnection = connection;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        if (logger.isDebugEnabled()) {
            logger.debug("channelActive: {}", ctx.channel());
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String jsonString = new String((byte[])msg);
        if (logger.isDebugEnabled()) {
            logger.debug("channelRead: {} -- > {} ", ctx.channel(), jsonString);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Error. ", cause.getCause());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.error("channelInactive: {}", ctx.channel());
        ctx.close();
        ClientProxyDelegate.instance.removeConnection(clientConnection);
        reconnect();
    }

    private void reconnect() {
        if (ClientProxyDelegate.instance.isStopped()){
            return;
        }

        logger.info("reconnect....");
        ClientProxyDelegate.instance.newConnection();
    }

}
