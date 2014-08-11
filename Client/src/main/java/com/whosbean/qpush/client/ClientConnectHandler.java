package com.whosbean.qpush.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by yaming_deng on 14-8-11.
 */
public class ClientConnectHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("channelActive: " + ctx.channel());
        ChannelHolder.save(ctx.channel());
    }

    protected void printMsg(Object msg){
        ByteBuf b = (ByteBuf)msg;
        byte[] dd = new byte[b.readableBytes()];
        b.readBytes(dd);
        String jsonString = new String(dd);
        System.out.println(jsonString);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {

        System.out.println("channelRead: " + ctx.channel());

        printMsg(msg);

        ReferenceCountUtil.release(msg);

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ChannelHolder.remove(ctx.channel());
        cause.printStackTrace();
        ctx.close();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelInactive();
        System.out.println("channelInactive: " + ctx.channel());
        ChannelHolder.remove(ctx.channel());
    }

}
