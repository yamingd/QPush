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

import java.util.List;
import java.util.concurrent.Callable;

/**
 *
 * 1对1或1对多的推送
 *
 * Created by yaming_deng on 14-8-8.
 */
public class OneSendThread implements Callable<Integer> {

    public static final String NULL = "NULL";
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

        List<String> clients = message.getClients();
        if (clients == null || clients.size() == 0){
            logger.error("Message Clients is Empty. {}", message);
            if (message.getStatusId().intValue() == PayloadStatus.Pending0) {
                message.setStatusId(PayloadStatus.Failed);
                message.setTotalUsers(0);
                PayloadServiceImpl.instance.add(message);
            }else{
                message.setStatusId(PayloadStatus.Failed);
                message.setTotalUsers(0);
                PayloadServiceImpl.instance.updateSendStatus(message);
            }
            return 0;
        }

        if (message.getStatusId().intValue() == PayloadStatus.Pending0) {
            message.setStatusId(PayloadStatus.Pending);
            message.setTotalUsers(0);
            PayloadServiceImpl.instance.add(message);
        }

        logger.info("OneSendThread. Client Total: {}", clients.size());
        for (int i = 0; i < clients.size(); i++) {
            String client = clients.get(i);
            Client cc = ClientServiceImpl.instance.findByUserId(client);
            if (cc == null){
                //离线
                logger.error("Client not found. client={}", client);
                if (message.getOfflineMode().intValue() == PBAPNSMessage.OfflineModes.SendAfterOnline_VALUE){
                    message.setStatus(client, new PushStatus(PushStatus.NoClient));
                }

                continue;
            }

            //显示在客户端
            message.setBadge(cc.getBadge() + 1);

            Connection c = ConnectionKeeper.get(product.getAppKey(), client);
            if(c != null) {
                if (ClientStatus.Online == cc.getStatusId()){
                    c.send(message);
                    message.setStatus(client, new PushStatus(PushStatus.TcpSent));
                }else{
                    sendMessageToOfflineClient(client, cc);
                }
            }else{
                sendMessageToOfflineClient(client, cc);
            }
        }

        PayloadServiceImpl.instance.updateSendStatus(message);

        return clients.size();

    }

    private void sendMessageToOfflineClient(String client, Client cc) {
        if (!cc.supportAPNS()){
            //不是iOS, 可以不继续跑
            logger.error("Client is not iOS. client={}, ", client);
            message.setStatus(cc.getUserId(), new PushStatus(PushStatus.NoConnections));
            return;
        }

        int offlineMode = message.getOfflineMode().intValue();
        if (StringUtils.isBlank(cc.getDeviceToken()) || NULL.equalsIgnoreCase(cc.getDeviceToken())){
            logger.error("Client's deviceToken not found. client={},", client);

            if (offlineMode == PBAPNSMessage.OfflineModes.SendAfterOnline_VALUE){
                message.setStatus(cc.getUserId(), new PushStatus(PushStatus.NO_DEVICE_TOKEN));
            }else{
                message.setStatus(cc.getUserId(), new PushStatus(PushStatus.Ignore));
            }

            return;
        }

        if (0 == message.getToMode()){
            if (offlineMode == PBAPNSMessage.OfflineModes.APNS_VALUE) {
                if (PBAPNSMessage.APNSModes.Signined_VALUE == message.getApnsMode()){
                    if (0 == cc.getStatusId()){
                        // 已退出
                        message.setStatus(cc.getUserId(), new PushStatus(PushStatus.WaitOnline));
                    }else{
                        APNSKeeper.instance.push(this.product, cc, message);
                    }
                }else {
                    APNSKeeper.instance.push(this.product, cc, message);
                }
            }else if (offlineMode == PBAPNSMessage.OfflineModes.SendAfterOnline_VALUE){
                message.setStatus(cc.getUserId(), new PushStatus(PushStatus.WaitOnline));
            }
        }else{
            message.setStatus(cc.getUserId(), new PushStatus(PushStatus.Ignore));
        }
    }

}
