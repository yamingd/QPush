package com.argo.qpush.gateway.handler;

import com.argo.qpush.core.MetricBuilder;
import com.argo.qpush.core.entity.Client;
import com.argo.qpush.core.service.ClientService;
import com.argo.qpush.gateway.Connection;
import com.argo.qpush.gateway.ServerConfig;
import com.argo.qpush.gateway.keeper.ConnectionKeeper;
import com.argo.qpush.protobuf.*;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Created by yaming_deng on 14-8-6.
 */
public class MobileMessageHandler extends ChannelInboundHandlerAdapter {

    protected static Logger logger = LoggerFactory.getLogger(MobileMessageHandler.class);

    private ThreadPoolTaskExecutor poolTaskExecutor;

    public MobileMessageHandler(){
        int limit = Integer.parseInt(ServerConfig.getConf().getProperty("handler.executors", "100"));

        poolTaskExecutor = new ThreadPoolTaskExecutor();
        poolTaskExecutor.setCorePoolSize(limit/10);
        poolTaskExecutor.setMaxPoolSize(limit);
        poolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        poolTaskExecutor.afterPropertiesSet();
    }

    /**
     * 接收到新的连接
     */
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws Exception {
        logger.info("channelActive: " + ctx.channel().hashCode());
    }

    /**
     * 读取新消息 LengthFieldBasedFrameDecoder 自动解包
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        logger.info("channelRead: " + ctx.channel().hashCode());
        MetricBuilder.requestMeter.mark();

        final PBAPNSEvent cc;

        try {
            byte[] bytes = (byte[]) msg;
            logger.info("Got Message, length:{}", bytes.length);
            cc = PBAPNSEvent.newBuilder().mergeFrom(bytes).build();
        } catch (Exception e) {
            logger.error("Invalid Data Package.", e);
            ack(ctx, null);
            return;
        }

        ReferenceCountUtil.release(msg);

        if (cc.getTypeId() == PBAPNSEvent.DeviceTypes.Android_VALUE){
            MetricBuilder.clientAndroidMeter.mark();
        }else if (cc.getTypeId() == PBAPNSEvent.DeviceTypes.iOS_VALUE){
            MetricBuilder.clientIOSMeter.mark();
        }

        if(cc.getOp() == PBAPNSEvent.Ops.Online_VALUE){
            ConnectionKeeper.add(cc.getAppKey(), cc.getUserId(), new Connection(ctx.channel()));
            poolTaskExecutor.submit(new OnNewlyAddThread(cc));
            ack(ctx, cc);
        }else if(cc.getOp() == PBAPNSEvent.Ops.KeepAlive_VALUE){
            //心跳
            ack(ctx, cc);
        }else if(cc.getOp() == PBAPNSEvent.Ops.PushAck_VALUE){
            //推送反馈
            ack(ctx, cc);
        }else if(cc.getOp() == PBAPNSEvent.Ops.Offline_VALUE){
            //离线
            poolTaskExecutor.submit(new Runnable() {
                @Override
                public void run() {
                    Client c0 = ClientService.instance.findByUserId(cc.getUserId());
                    if (c0 != null){
                        ClientService.instance.updateOnlineTs(c0.getId());
                    }
                }
            });
            ctx.close();
        }
    }

    private void ack(final ChannelHandlerContext ctx, PBAPNSEvent cc){
        PBAPNSMessage.Builder builder = PBAPNSMessage.newBuilder();
        builder.setAps(PBAPNSBody.newBuilder().setAlert("ack").setBadge(0));
        if (cc != null) {
            builder.addUserInfo(PBAPNSUserInfo.newBuilder().setKey("op").setValue(cc.getOp() + ""));
        }else{
            builder.addUserInfo(PBAPNSUserInfo.newBuilder().setKey("op").setValue("5"));
        }
        byte[] bytes = builder.build().toByteArray();

        final ByteBuf data = ctx.alloc().buffer(bytes.length); // (2)
        data.writeBytes(bytes);
        final ChannelFuture cf = ctx.channel().writeAndFlush(data);
        cf.addListener(new GenericFutureListener<Future<? super Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(cf.cause() != null){
                    logger.error("Send Error.", cf.cause());
                    ctx.close();
                }
            }
        });
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelReadComplete: " + ctx.channel().hashCode());
        ctx.flush();
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
        ConnectionKeeper.remove(ctx.channel().hashCode());
    }

}
