package com.whosbean.qpush.client;

import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Created by yaming_deng on 14-8-11.
 */
public class QPushClient {

    protected static Logger logger = LoggerFactory.getLogger(QPushClient.class);

    public static boolean send(AppPayload payload) throws IOException {
        Channel c = ClientProxyDelegate.instance.get();
        byte[] bytes = ClientProxyDelegate.messagePack.write(payload);
        final ByteBuf data = c.config().getAllocator().buffer(bytes.length); // (2)
        data.writeBytes(bytes);
        ChannelFuture cf = c.writeAndFlush(data);
        if(cf.isDone()){
            if(cf.cause() != null){
                cf.cause().printStackTrace();
                c.close();
                ClientProxyDelegate.instance.remove(c);
                return false;
            }
            return true;
        }
        return false;
    }
}
