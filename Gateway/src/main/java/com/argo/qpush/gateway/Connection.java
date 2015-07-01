package com.argo.qpush.gateway;

import com.argo.qpush.core.entity.Payload;
import com.argo.qpush.core.entity.PushStatus;
import com.argo.qpush.core.service.ClientServiceImpl;
import com.argo.qpush.core.service.PayloadServiceImpl;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * Created by yaming_deng on 14-8-6.
 */
public class Connection {

    public static long epoch = 1420041600L;

    protected Logger logger = null;

    private final Channel channel;
    private String appKey;
    private String userId;
    private int lastOpTime;

    public Connection(Channel channel) {
        this.channel = channel;
        logger = LoggerFactory.getLogger(Connection.class.getName() + ".Channel." + channel.hashCode());
        lastOpTime = (int) (new Date().getTime() / 1000 - epoch);
    }

    public int getLastOpTime() {
        return lastOpTime;
    }

    /**
     * 更新最后发送时间
     */
    public void updateOpTime(){
        lastOpTime = (int) (new Date().getTime() / 1000 - epoch);
    }

    /**
     * 发送消息
     * @param message
     */
    public void send(final Payload message){
        // 组装消息包
        if(channel.isOpen()){
            try {
                byte[] msg = message.asAPNSMessage().toByteArray();
                send(message, msg);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                message.setStatus(this.userId, new PushStatus(PushStatus.UnKnown, e.getMessage()));
            }
        }else{
            message.setStatus(this.userId, new PushStatus(PushStatus.ChannelClosed, null));
            logger.error("Send Error. Channel is closed. {}, {}", channel, message);
        }
    }

    /**
     * 发送消息
     * @param message
     * @param msg
     */
    public void send(final Payload message, final byte[] msg) {
        try {
            final ByteBuf data = channel.config().getAllocator().buffer(msg.length); // (2)
            data.writeBytes(msg);
            final ChannelPromise cf = channel.newPromise();
            cf.addListener(new GenericFutureListener<Future<? super Void>>() {
                @Override
                public void operationComplete(Future<? super Void> future) throws Exception {
                    if(cf.cause() != null){
                        logger.error("{}, Send Error.", channel, cf.cause());
                        PayloadServiceImpl.instance.updateSendStatus(message, userId, new PushStatus(PushStatus.WriterError, cf.cause().getMessage()));
                    }else {
                        updateOpTime();
                        PayloadServiceImpl.instance.updateSendStatus(message, userId, new PushStatus(PushStatus.Success));
                        ClientServiceImpl.instance.updateBadge(userId, 1);
                        if (logger.isDebugEnabled()){
                            logger.debug("Send Done, userId={}, messageId={}", userId, message.getId());
                        }
                    }
                }
            });

            channel.writeAndFlush(data, cf);

        } catch (Exception e) {
            message.setStatus(userId, new PushStatus(PushStatus.UnKnown, e.getMessage()));
            logger.error(e.getMessage(), e);
        }
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void close(){
        channel.close();
    }

    public Channel getChannel() {
        return channel;
    }

    @Override
    public int hashCode() {
        return channel.hashCode();
    }

    @Override
    public String toString() {
        return "Connection{" +
                "channel=" + channel +
                ", appKey='" + appKey + '\'' +
                ", userId='" + userId + '\'' +
                ", lastOpTime=" + lastOpTime +
                '}';
    }
}
