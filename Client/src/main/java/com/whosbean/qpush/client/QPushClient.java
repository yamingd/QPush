package com.whosbean.qpush.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by yaming_deng on 14-8-11.
 */
public class QPushClient {

    protected static Logger logger = LoggerFactory.getLogger(QPushClient.class);

    public static void start() {
        ClientProxyDelegate.instance.start();
    }

    public static boolean send(final AppPayload payload) throws IOException {
        final Channel c = ClientProxyDelegate.instance.get();
        byte[] bytes = ClientProxyDelegate.messagePack.write(payload);
        final ByteBuf data = c.config().getAllocator().buffer(bytes.length); // (2)
        data.writeBytes(bytes);
        final ChannelFuture cf = c.writeAndFlush(data);
        cf.addListener(new GenericFutureListener<Future<? super java.lang.Void>>() {
            @Override
            public void operationComplete(Future<? super Void> future) throws Exception {
                if(cf.cause() != null){
                    logger.error("Send Error: " + payload + "\n", cf.cause());
                    c.close();
                    ClientProxyDelegate.instance.remove(c);
                }
            }
        });
        return true;
    }
}
