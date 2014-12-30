package com.argo.qpush.gateway.handler;

import com.argo.qpush.core.entity.Client;
import com.argo.qpush.core.entity.Product;
import com.argo.qpush.core.service.ClientService;
import com.argo.qpush.core.service.ProductService;
import com.argo.qpush.gateway.dispatch.Dispatcher;
import com.argo.qpush.gateway.dispatch.DispatcherRunner;
import com.argo.qpush.protobuf.PBAPNSEvent;

import java.util.concurrent.Callable;

/**
 * 触发新增客户端.
 * Created by yaming_deng on 14-8-11.
 */
public class OnNewlyAddThread implements Callable<Boolean> {

    private PBAPNSEvent cc;
    public OnNewlyAddThread(PBAPNSEvent cc){
        this.cc = cc;
    }

    @Override
    public Boolean call() throws Exception {
        Client client = ClientService.instance.findByUserId(cc.getUserId());
        boolean isnew = false;
        if (client == null){
            client = new Client();
            Product product = ProductService.instance.findByKey(cc.getAppKey());
            client.setProductId(product.getId());
            client.setUserId(cc.getUserId());
            client.setTypeId(cc.getTypeId()); //
            client.setDeviceToken(cc.getToken());
            try {
                ClientService.instance.add(client);
            } catch (Exception e) {
                e.printStackTrace();
            }
            isnew = true;
        }
        //推送旧消息
        if (!isnew) {
            Dispatcher dispatcher = DispatcherRunner.instance.get(cc.getAppKey());
            if (dispatcher != null) {
                dispatcher.pushOfflinePayload(cc.getUserId());
            }
            try {
                ClientService.instance.updateOnlineTs(client.getId());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return true;
    }
}
