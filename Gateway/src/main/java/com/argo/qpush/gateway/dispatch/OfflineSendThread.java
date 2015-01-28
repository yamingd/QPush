package com.argo.qpush.gateway.dispatch;

import com.argo.qpush.core.entity.Payload;
import com.argo.qpush.core.entity.Product;
import com.argo.qpush.core.service.PayloadService;
import com.argo.qpush.gateway.Connection;
import com.argo.qpush.gateway.SentProgress;
import com.argo.qpush.gateway.keeper.ConnectionKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 *
 * 1对1或1对多的推送
 *
 * Created by yaming_deng on 14-8-8.
 */
public class OfflineSendThread implements Callable<Integer> {

    protected static Logger logger = LoggerFactory.getLogger(OfflineSendThread.class);

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
                progress.getCountDownLatch().await();
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }

            int total = progress.getSuccess().get();

            try {
                PayloadService.instance.updateSendStatus(message, total);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            return total;

        }

        return 0;
    }
}
