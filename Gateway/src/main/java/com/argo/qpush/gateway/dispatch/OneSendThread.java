package com.argo.qpush.gateway.dispatch;

import com.argo.qpush.core.entity.*;
import com.argo.qpush.core.service.ClientServiceImpl;
import com.argo.qpush.core.service.PayloadServiceImpl;
import com.argo.qpush.gateway.Connection;
import com.argo.qpush.gateway.keeper.APNSKeeper;
import com.argo.qpush.gateway.keeper.ConnectionKeeper;
import com.argo.qpush.protobuf.PBAPNSMessage;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 *
 * 1对1或1对多的推送
 *
 * Created by yaming_deng on 14-8-8.
 */
public class OneSendThread implements Callable<Integer> {

    protected static Logger logger = LoggerFactory.getLogger(OneSendThread.class);

    private Payload message;
    private Product product;

    public OneSendThread(final Product product, final Payload message) {
        super();
        this.message = message;
        this.product = product;
    }

    @Override
    public Integer call() throws Exception {
        long ts0 = System.currentTimeMillis();
        int ret = 0;
        try {
            ret = doSend();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        long duration = System.currentTimeMillis() - ts0;
        logger.info("Send Duration={} ms. total={}, ", duration, ret);
        return ret;
    }

    private Integer doSend() throws Exception {

        if(message == null){
            return 0;
        }

        if (message.getClients() == null || message.getClients().size() == 0){
            logger.error("Message Clients is Empty. {}", message);
            message.setStatusId(PayloadStatus.Failed);
            message.setTotalUsers(0);
            PayloadServiceImpl.instance.add(message);
            return 0;
        }

        if (message.getStatusId().intValue() == PayloadStatus.Pending0) {
            message.setStatusId(PayloadStatus.Pending);
            message.setTotalUsers(0);
            PayloadServiceImpl.instance.add(message);
        }

        for (String client : message.getClients()){

            Client cc = ClientServiceImpl.instance.findByUserId(client);
            if (cc == null){
                //离线
                logger.error("Client not found. client=" + client);
                if (message.getOfflineMode().intValue() == PBAPNSMessage.OfflineModes.SendAfterOnline_VALUE){
                    message.setStatus(client, new PushStatus(PushStatus.NoClient));
                }

                continue;
            }

            //显示在客户端
            message.setBadge(cc.getBadge() + 1);

            Connection c = ConnectionKeeper.get(product.getAppKey(), client);
            if(c != null) {
                c.send(message);
            }else{

                if (!cc.isDevice(ClientType.iOS)){
                    //不是iOS, 可以不继续跑
                    logger.error("Client is not iOS. client=" + client);
                    message.setStatus(cc.getUserId(), new PushStatus(PushStatus.NoConnections));
                    continue;
                }

                if (StringUtils.isBlank(cc.getDeviceToken()) || "NULL".equalsIgnoreCase(cc.getDeviceToken())){
                    logger.error("Client's deviceToken not found. client=" + client);

                    if (message.getOfflineMode().intValue() == PBAPNSMessage.OfflineModes.SendAfterOnline_VALUE){
                        message.setStatus(cc.getUserId(), new PushStatus(PushStatus.NO_DEVICE_TOKEN));
                    }else{
                        message.setStatus(cc.getUserId(), new PushStatus(PushStatus.Ignore));
                    }

                    continue;
                }

                if (0 == message.getToMode()){
                    if (message.getOfflineMode().intValue() == PBAPNSMessage.OfflineModes.APNS_VALUE) {
                        APNSKeeper.instance.push(this.product, cc, message);
                    }else if (message.getOfflineMode().intValue() == PBAPNSMessage.OfflineModes.SendAfterOnline_VALUE){
                        message.setStatus(cc.getUserId(), new PushStatus(PushStatus.WaitOnline));
                    }
                }else{
                    message.setStatus(cc.getUserId(), new PushStatus(PushStatus.Ignore));
                }

            }
        }

        PayloadServiceImpl.instance.updateSendStatus(message);

        return message.getClients().size();

    }

}
