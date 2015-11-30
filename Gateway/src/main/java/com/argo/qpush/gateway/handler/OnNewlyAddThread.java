package com.argo.qpush.gateway.handler;

import com.argo.qpush.core.entity.Client;
import com.argo.qpush.core.entity.Product;
import com.argo.qpush.core.service.ClientServiceImpl;
import com.argo.qpush.core.service.ProductServiceImpl;
import com.argo.qpush.gateway.dispatch.Dispatcher;
import com.argo.qpush.gateway.dispatch.DispatcherRunner;
import com.argo.qpush.protobuf.PBAPNSEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 * 触发新增客户端.
 * Created by yaming_deng on 14-8-11.
 */
public class OnNewlyAddThread implements Callable<Boolean> {

    protected static Logger logger = LoggerFactory.getLogger(OnNewlyAddThread.class);

    private PBAPNSEvent cc;
    public OnNewlyAddThread(PBAPNSEvent cc){
        this.cc = cc;
    }

    @Override
    public Boolean call() throws Exception {
        Client client = ClientServiceImpl.instance.findByUserId(cc.getUserId());
        boolean isNew = false;
        if (client == null){
            client = new Client();
            Product product = ProductServiceImpl.instance.findByKey(cc.getAppKey());
            client.setProductId(product.getId());
            client.setUserId(cc.getUserId());
            client.setTypeId(cc.getTypeId()); //
            client.setDeviceToken(cc.getToken());
            client.setDeviceId(cc.getDeviceId());
            try {
                ClientServiceImpl.instance.add(client);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            isNew = true;
        }

        //如不是旧客户端. 则推送旧消息
        if (!isNew) {
            client.setDeviceToken(cc.getToken());
            client.setTypeId(cc.getTypeId());

            Dispatcher dispatcher = DispatcherRunner.instance.get(cc.getAppKey());
            if (dispatcher != null) {
                dispatcher.pushOfflinePayload(cc.getUserId());
            }
            try {
                ClientServiceImpl.instance.updateOnlineTs(client);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return true;
    }
}
