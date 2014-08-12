package com.whosbean.qpush.gateway.dispatch;

import com.whosbean.qpush.core.MetricBuilder;
import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.core.entity.Product;
import com.whosbean.qpush.core.service.PayloadService;
import com.whosbean.qpush.gateway.Connection;
import com.whosbean.qpush.gateway.keeper.ConnectionKeeper;

import java.util.concurrent.Callable;

/**
 *
 * 1对1或1对多的推送
 *
 * Created by yaming_deng on 14-8-8.
 */
public class OneSendThread implements Callable<Boolean> {

    private long messageId;
    private Product product;

    public OneSendThread(Product product, long messageId) {
        super();
        this.messageId = messageId;
        this.product = product;
    }

    @Override
    public Boolean call() throws Exception {
        Payload message = PayloadService.instance.get(this.messageId);
        if(message == null){
            return true;
        }
        boolean ok = false;
        if(message.getClients()!=null){
            for (String client : message.getClients()){
                Connection c = ConnectionKeeper.get(product.getKey(), client);
                ok = c.send(message);
                MetricBuilder.pushMeter.mark();
                MetricBuilder.pushSingleMeter.mark();
            }
            PayloadService.instance.addHisotry(message, null, message.getClients().size(), ok);
        }
        return ok;
    }
}
