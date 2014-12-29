package com.argo.qpush.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by yaming_deng on 14-8-11.
 */
public class ClientConnectHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive: " + ctx.channel());
        ClientProxyDelegate.instance.save(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String jsonString = new String((byte[])msg);
        System.out.println("channelRead: " + ctx.channel() + " --> " + jsonString);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ClientProxyDelegate.instance.remove(ctx.channel());
        cause.printStackTrace();
        ctx.close();
        reconnect();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        System.out.println("channelInactive: " + ctx.channel());
        ClientProxyDelegate.instance.remove(ctx.channel());
        reconnect();
    }

    private void reconnect() {
        if (ClientProxyDelegate.instance.isStopped()){
            return;
        }

        ClientProxyDelegate.instance.newChannel();
    }

}
