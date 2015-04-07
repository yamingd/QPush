package com.argo.qpush.gateway.handler;

import com.argo.qpush.core.MetricBuilder;
import com.argo.qpush.core.entity.Client;
import com.argo.qpush.core.entity.ClientStatus;
import com.argo.qpush.core.service.ClientServiceImpl;
import com.argo.qpush.gateway.Connection;
import com.argo.qpush.gateway.keeper.ConnectionKeeper;
import com.argo.qpush.protobuf.PBAPNSBody;
import com.argo.qpush.protobuf.PBAPNSEvent;
import com.argo.qpush.protobuf.PBAPNSMessage;
import com.argo.qpush.protobuf.PBAPNSUserInfo;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yaming_deng on 14-8-6.
 */
public class MobileMessageHandler extends ChannelInboundHandlerAdapter {

    protected static Logger logger = LoggerFactory.getLogger(MobileMessageHandler.class);

    public MobileMessageHandler(){

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
        if (logger.isDebugEnabled()) {
            logger.info("channelRead: " + ctx.channel().hashCode());
        }
        MetricBuilder.requestMeter.mark();

        final PBAPNSEvent cc;

        try {
            byte[] bytes = (byte[]) msg;
            if(logger.isDebugEnabled()){
                logger.debug("Got Message, length:{}", bytes.length);
                logger.debug("bytes: {}", bytes);
            }
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
            if (logger.isDebugEnabled()){
                logger.debug("Got Online Message. {}", cc);
            }
            Connection conn = new Connection(ctx.channel());
            conn.setUserId(cc.getUserId());
            conn.setAppKey(cc.getAppKey());
            ConnectionKeeper.add(cc.getAppKey(), cc.getUserId(), conn);
            MessageHandlerPoolTasks.instance.getExecutor().submit(new OnNewlyAddThread(cc));
            ack(ctx, cc);
            if (logger.isDebugEnabled()){
                logger.debug("Got Online Message and handle DONE. {}", cc);
            }
        }else if(cc.getOp() == PBAPNSEvent.Ops.KeepAlive_VALUE){
            //心跳
            ack(ctx, cc);
        }else if(cc.getOp() == PBAPNSEvent.Ops.PushAck_VALUE){
            //推送反馈
            ack(ctx, cc);
        }else if(cc.getOp() == PBAPNSEvent.Ops.Offline_VALUE){
            //离线
            MessageHandlerPoolTasks.instance.getExecutor().submit(new Runnable() {
                @Override
                public void run() {
                    Client c0 = ClientServiceImpl.instance.findByUserId(cc.getUserId());
                    if (c0 != null){
                        ClientServiceImpl.instance.updateStatus(c0.getId(), ClientStatus.Offline);
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
            builder.addUserInfo(PBAPNSUserInfo.newBuilder().setKey("op").setValue(cc.getOp() + "").setKey("kindId").setValue("sync"));
        }else{
            builder.addUserInfo(PBAPNSUserInfo.newBuilder().setKey("op").setValue("5").setKey("kindId").setValue("sync"));
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
        Connection connection = ConnectionKeeper.get(ctx.channel().hashCode());
        if (null != connection){
            Client client = ClientServiceImpl.instance.findByUserId(connection.getUserId());
            if (null != client){
                logger.info("Client offline: {}", client);
                ClientServiceImpl.instance.updateStatus(client.getId(), ClientStatus.Offline);
            }
        }
        ConnectionKeeper.remove(ctx.channel().hashCode());
    }

}
