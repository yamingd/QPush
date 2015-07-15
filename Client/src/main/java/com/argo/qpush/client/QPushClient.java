package com.argo.qpush.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
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

    public static void connect() {
        ClientProxyDelegate.instance.start();
    }

    public static void close(){
        ClientProxyDelegate.instance.close();
    }

    public static boolean send(final AppPayload payload) throws IOException {
        final byte[] bytes = ClientProxyDelegate.messagePack.write(payload);

        trySend(payload, bytes, 3);

        return true;
    }

    private static void trySend(final AppPayload payload, final byte[] bytes, final int limit) {
        if (limit <= 0){
            logger.error("TrySend Failure.");
            return;
        }

        ClientProxyDelegate.instance.get(new ChannelAvailable() {

           @Override
           public void execute(final ChannelHandlerContext c) {
               final ByteBuf data = c.alloc().buffer(bytes.length); // (2)
               data.writeBytes(bytes);
               final ChannelFuture cf = c.writeAndFlush(data);
               cf.addListener(new GenericFutureListener<Future<? super Void>>() {
                   @Override
                   public void operationComplete(Future<? super Void> future) throws Exception {
                       if(cf.cause() != null){
                           logger.error("Send Error: " + payload + "\n", cf.cause());
                           c.close();
                           ClientProxyDelegate.instance.remove(c);
                           ClientProxyDelegate.instance.newChannel();
                           trySend(payload, bytes, limit-1);
                       }else{
                           logger.info("Send OK: " + payload + "\n");
                       }
                   }
               });
           }
       });
    }
}
