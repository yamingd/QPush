package com.whosbean.qpush.publisher.handler;

import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.publisher.queue.DisruptorContext;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.msgpack.MessagePack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by yaming_deng on 14-8-6.
 */
public class PublisherConnHandler extends ChannelInboundHandlerAdapter {

    protected static Logger logger = LoggerFactory.getLogger(PublisherConnHandler.class);
    protected MessagePack messagePack = new MessagePack();

    /**
     * 接收到新的连接
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        logger.info("channelActive: " + ctx.channel().hashCode());
    }

    /**
     * 读取新消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.info("channelRead: " + ctx.channel().hashCode());
        ByteBuf b = (ByteBuf)msg;
        byte[] dd = new byte[b.readableBytes()];
        b.readBytes(dd);

        try {
            Payload payload = messagePack.read(dd, Payload.class);
            ReferenceCountUtil.release(msg);
            DisruptorContext.producer.push(payload);
            ack(ctx, "200");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            ack(ctx, "500");
        }
    }

    private void ack(ChannelHandlerContext ctx, String msg) {
        //回复客户端.
        byte[] bytes = msg.getBytes();
        final ByteBuf data = ctx.alloc().buffer(bytes.length); // (2)
        data.writeBytes(bytes);
        ChannelFuture cf = ctx.channel().writeAndFlush(data);
        if(cf.isDone() && cf.cause() != null){
            cf.cause().printStackTrace();
            ctx.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelReadComplete: " + ctx.channel().hashCode());
    }

    /**
     * 连接异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

    /**
     * 连接断开，移除连接影射，客户端发起重连
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx)
            throws Exception {
        logger.info("channelInactive: " + ctx.channel().hashCode());
    }

}
