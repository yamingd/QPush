package com.whosbean.qpush.publisher.handler;

import com.whosbean.qpush.client.PayloadMessage;
import com.whosbean.qpush.core.MessageUtils;
import com.whosbean.qpush.core.MetricBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by yaming_deng on 14-8-6.
 */
public class PublisherConnHandler extends ChannelInboundHandlerAdapter {

    protected static Logger logger = LoggerFactory.getLogger(PublisherConnHandler.class);

    public PublisherConnHandler() {
    }

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

        MetricBuilder.recvMeter.mark();

        byte[] dd = (byte[])msg;

        try {
            PayloadMessage message = MessageUtils.asT(PayloadMessage.class, dd);
            if (logger.isDebugEnabled()){
                logger.debug("Payload. message={}", message);
            }
            PayloadHandler.instance.save(message);
            ack(ctx, "200");
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            ack(ctx, "500");
        }finally {
            ctx.fireChannelRead(msg);
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
