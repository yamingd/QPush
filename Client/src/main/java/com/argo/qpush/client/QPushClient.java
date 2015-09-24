package com.argo.qpush.client;

import io.netty.channel.ChannelFuture;
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

    /**
     * &#x53d1;&#x9001;Payload
     * @param appKey
     * @param payload
     * @return boolean
     * @throws IOException
     */
    public static boolean sendPayload(final String appKey, final AppPayload payload) throws IOException {

        AppRequest request = new AppRequest();
        request.setAppkey(appKey);
        request.setTypeId(AppRequest.APP_REQUEST_TYPE_PAYLOAD);

        byte[] bytes = ClientProxyDelegate.messagePack.write(payload);
        request.setData(bytes);

        bytes = ClientProxyDelegate.messagePack.write(request);
        trySend(payload.toString(), bytes, 3);

        return true;
    }

    /**
     * 发送Topic操作
     * @param appKey
     * @param payload
     * @return
     * @throws IOException
     */
    public static boolean sendTopic(final String appKey, int typeId, final AppTopic payload) throws IOException {

        AppRequest request = new AppRequest();
        request.setAppkey(appKey);
        request.setTypeId(typeId);

        byte[] bytes = ClientProxyDelegate.messagePack.write(payload);
        request.setData(bytes);

        bytes = ClientProxyDelegate.messagePack.write(request);
        trySend(payload.toString(), bytes, 3);

        return true;
    }

    private static void trySend(final String payload, final byte[] bytes, final int limit) {
        if (limit <= 0){
            logger.error("TrySend Failure.");
            return;
        }

        ClientProxyDelegate.instance.get(new ChannelAvailable() {

           @Override
           public void execute(final ClientConnection c) {

               c.send(bytes, new GenericFutureListener<ChannelFuture>() {

                   @Override
                   public void operationComplete(ChannelFuture future) throws Exception {
                       if (!future.isSuccess()) {
                           logger.error("Send Error: " + payload + "\n", future.cause());
                           trySend(payload, bytes, limit - 1);
                       } else {
                           //logger.info("Send OK: " + payload + "\n");
                       }
                   }
               });
           }

            @Override
            public void error(Exception e) {
                trySend(payload, bytes, limit-1);
            }
        });
    }
}
