package com.argo.qpush.publisher.handler;

import com.argo.qpush.client.PayloadMessage;
import com.argo.qpush.core.MessageUtils;
import com.argo.qpush.core.MetricBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by yaming_deng on 14-8-6.
 */
public class PublisherConnHandler extends ChannelInboundHandlerAdapter {

    public static final String STATUS_200 = "200";
    public static final String STATUS_500 = "500";

    protected static Logger logger = LoggerFactory.getLogger(PublisherConnHandler.class);

    public PublisherConnHandler() {
    }

    /**
     * 接收到新的连接
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        logger.info("channelActive: {}", ctx.channel().hashCode());
    }

    /**
     * 读取新消息
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.info("channelRead: {}", ctx.channel().hashCode());

        MetricBuilder.recvMeter.mark();

        byte[] dd = (byte[])msg;

        try {
            PayloadMessage message = MessageUtils.asT(PayloadMessage.class, dd);
            if (logger.isDebugEnabled()){
                logger.debug("Payload. message={}", message);
            }
            PayloadHandler.instance.save(message);
            ack(ctx, STATUS_200);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            ack(ctx, STATUS_500);
        }
    }

    private void ack(final ChannelHandlerContext ctx, String msg) {
        //回复客户端.
        byte[] bytes = msg.getBytes();
        final ByteBuf data = ctx.alloc().buffer(bytes.length); // (2)
        data.writeBytes(bytes);
        final ChannelFuture cf = ctx.channel().writeAndFlush(data);
        cf.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(cf.cause() != null){
                    logger.error("Ack Error.", cf.cause());
                    ctx.close();
                }
            }
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelReadComplete: {}", ctx.channel().hashCode());
        ctx.flush();
    }

    /**
     * 连接异常
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("channel Error: {}, {}", ctx.channel().hashCode(), cause);
        ctx.close();
    }

    /**
     * 连接断开，移除连接影射，客户端发起重连
     */
    @Override
    public void channelInactive(ChannelHandlerContext ctx)
            throws Exception {
        logger.info("channelInactive: {}", ctx.channel().hashCode());
        ctx.close();
    }

}
