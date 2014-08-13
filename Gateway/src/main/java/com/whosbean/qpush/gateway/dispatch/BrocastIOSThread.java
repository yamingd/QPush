package com.whosbean.qpush.gateway.dispatch;

import com.notnoop.apns.ApnsService;
import com.whosbean.qpush.core.GsonUtils;
import com.whosbean.qpush.core.MetricBuilder;
import com.whosbean.qpush.core.entity.Client;
import com.whosbean.qpush.core.entity.ClientType;
import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.core.entity.Product;
import com.whosbean.qpush.core.service.ClientService;
import com.whosbean.qpush.core.service.PayloadService;
import com.whosbean.qpush.gateway.keeper.APNSKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.Callable;

/**
 * iOS广播推送.
 * Created by yaming_deng on 14-8-8.
 */
public class BrocastIOSThread implements Callable<Boolean> {

    protected static Logger logger = LoggerFactory.getLogger(BrocastIOSThread.class);

    private long messageId;
    private int start = 0;
    private int limit = 100;
    private Product product;

    public BrocastIOSThread(Product product, long messageId, int start, int limit) {
        super();
        this.messageId = messageId;
        this.start = start;
        this.limit = limit;
        this.product = product;
    }

    @Override
    public Boolean call() throws Exception {
        Payload message = PayloadService.instance.get(this.messageId);
        if(message == null){
            return true;
        }
        if(message.getClients() == null || message.getClients().size() == 0){
            List<Client> clients = ClientService.instance.findOfflineByType(this.product.getId(), ClientType.iOS, this.start, this.limit);
            for (Client c : clients){
                this.pushToApple(c, message);
            }
            try {
                PayloadService.instance.addHisotry(message, null, clients.size(), true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if (clients.size() > 0) {
                MetricBuilder.pushMeter.mark(clients.size());
                MetricBuilder.boradcastMeter.mark(clients.size());
            }
            logger.info("push to Apple. total = " + clients.size());
        }

        return true;
    }

    private void pushToApple(Client cc, Payload message){
        String json = GsonUtils.toJson(message.asStdMap());
        ApnsService service = APNSKeeper.get(this.product);
        if (service != null){
            service.push(cc.getDeviceToken(), json);
        }
    }

}
