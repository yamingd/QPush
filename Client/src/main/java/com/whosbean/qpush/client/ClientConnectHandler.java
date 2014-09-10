package com.whosbean.qpush.client;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Created by yaming_deng on 14-8-11.
 */
public class ClientConnectHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive: " + ctx.channel());
        QPushClient.save(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        String jsonString = new String((byte[])msg);
        System.out.println("channelRead: " + ctx.channel() + " --> " + jsonString);
        ctx.fireChannelRead(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        QPushClient.remove(ctx.channel());
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        System.out.println("channelInactive: " + ctx.channel());
        QPushClient.remove(ctx.channel());
    }

}
