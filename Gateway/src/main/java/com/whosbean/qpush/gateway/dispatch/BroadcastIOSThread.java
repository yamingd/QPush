package com.whosbean.qpush.gateway.dispatch;

import com.whosbean.qpush.core.MetricBuilder;
import com.whosbean.qpush.core.entity.Client;
import com.whosbean.qpush.core.entity.ClientType;
import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.core.entity.Product;
import com.whosbean.qpush.core.service.ClientService;
import com.whosbean.qpush.core.service.PayloadService;
import com.whosbean.qpush.gateway.SentProgress;
import com.whosbean.qpush.gateway.keeper.APNSKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * iOS广播推送.
 * Created by yaming_deng on 14-8-8.
 */
public class BroadcastIOSThread implements Callable<Integer> {

    protected static Logger logger = LoggerFactory.getLogger(BroadcastIOSThread.class);

    private Payload message;
    private int start = 0;
    private int limit = 100;
    private Product product;

    public BroadcastIOSThread(final Product product, final Payload message, int start, int limit) {
        super();
        this.message = message;
        this.start = start;
        this.limit = limit;
        this.product = product;
    }

    @Override
    public Integer call() throws Exception {
        if(message == null){
            return 0;
        }
        if(message.getClients() == null || message.getClients().size() == 0){
            List<Client> clients = ClientService.instance.findOfflineByType(this.product.getId(), ClientType.iOS, this.start, this.limit);
            SentProgress progress = new SentProgress(clients.size());
            for (Client c : clients){
                APNSKeeper.push(progress, this.product, c, message);
            }

            try {
                progress.getCountDownLatch().await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            int total = progress.getSuccess().get();
            try {
                PayloadService.instance.updateSendStatus(message, total);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (total > 0) {
                MetricBuilder.pushMeter.mark(total);
                MetricBuilder.boradcastMeter.mark(total);
            }
            logger.info("push to Apple. total = " + total);

            return total;
        }

        return 0;
    }
}
