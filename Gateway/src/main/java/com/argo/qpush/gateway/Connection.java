package com.argo.qpush.gateway;

import com.argo.qpush.core.entity.Payload;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yaming_deng on 14-8-6.
 */
public class Connection {

    protected static Logger logger = LoggerFactory.getLogger(Connection.class);

    private final Channel channel;

    public Connection(Channel channel) {
        this.channel = channel;
    }

    public void send(final SentProgress progress, Payload message) throws Exception {
        // 组装消息包
        if(channel.isOpen()){
            byte[] msg = message.asAPNSMessage().toByteArray();
            send(progress, msg);
        }else{
            progress.incrFailed();
            logger.error("Send Error. Channel is closed. {}", message);
        }
    }

    public void send(final SentProgress progress, final byte[] msg) {
        final ByteBuf data = channel.config().getAllocator().buffer(msg.length); // (2)
        data.writeBytes(msg);
        final ChannelFuture cf = channel.writeAndFlush(data);
        cf.addListener(new GenericFutureListener<Future<? super java.lang.Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(cf.cause() != null){
                    logger.error("Send Error.", cf.cause());
                    progress.incrFailed();
                }else {
                    progress.incrSuccess();
                }
            }
        });
    }

    public void close(){
        channel.close();
    }

    public Channel getChannel() {
        return channel;
    }

}
