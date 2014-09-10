package com.whosbean.qpush.gateway.dispatch;

import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.core.entity.Product;
import com.whosbean.qpush.core.service.PayloadService;
import com.whosbean.qpush.gateway.Connection;
import com.whosbean.qpush.gateway.SentProgress;
import com.whosbean.qpush.gateway.keeper.ConnectionKeeper;

import java.util.concurrent.Callable;

/**
 *
 * 1对1或1对多的推送
 *
 * Created by yaming_deng on 14-8-8.
 */
public class OfflineSendThread implements Callable<Integer> {

    private String userId;
    private Product product;

    public OfflineSendThread(Product product, String userId) {
        super();
        this.userId = userId;
        this.product = product;
    }

    @Override
    public Integer call() throws Exception {
        Payload message = PayloadService.instance.findLatest(product.getId(), userId);
        if(message == null){
            return 0;
        }

        if(message.getClients()!=null){
            SentProgress progress = new SentProgress(message.getClients().size());
            for (String client : message.getClients()){
                Connection c = ConnectionKeeper.get(product.getAppKey(), client);
                if(c != null) {
                    c.send(progress, message);
                }else{
                    progress.incrFailed();
                }
            }

            try {
                progress.getCountDownLatch().wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int total = progress.getSuccess().get();

            try {
                PayloadService.instance.updateSendStatus(message, total);
            } catch (Exception e) {
                e.printStackTrace();
            }

            return total;

        }

        return 0;
    }
}
