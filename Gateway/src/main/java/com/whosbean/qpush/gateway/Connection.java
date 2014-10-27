package com.whosbean.qpush.gateway;

import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.protobuf.convertor.PBConvertor;
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
            byte[] msg = PBConvertor.fromBean(message).toByteArray();
            send(progress, msg);
        }else{
            logger.error("Send Error. Channel is closed.");
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
