package com.whosbean.qpush.gateway.dispatch;

import com.whosbean.qpush.core.MetricBuilder;
import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.core.entity.Product;
import com.whosbean.qpush.core.service.PayloadService;
import com.whosbean.qpush.gateway.Connection;
import com.whosbean.qpush.gateway.keeper.ClientKeeper;
import com.whosbean.qpush.gateway.keeper.ConnectionKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * 广播推送.
 * Created by yaming_deng on 14-8-8.
 */
public class BrocastThread implements Callable<Boolean> {

    protected static Logger logger = LoggerFactory.getLogger(BrocastThread.class);

    private long messageId;
    private int start = 0;
    private int limit = 100;
    private Product product;

    public BrocastThread(Product product, long messageId, int start, int limit) {
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
            Collection<Integer> cids = ClientKeeper.gets(product.getKey());
            int s = start * limit;
            if(cids.size() < limit){
                limit = cids.size();
            }
            Integer[] temp = cids.toArray(new Integer[0]);
            int t0 = 0;
            for(int i=0; i<limit; i++){
                int cid = temp[s+i];
                Connection c = ConnectionKeeper.get(cid);
                if(c!=null){
                    c.send(message);
                    t0++;
                }
            }

            try {
                PayloadService.instance.addHisotry(message, null, t0, true);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (t0 > 0) {
                MetricBuilder.pushMeter.mark(t0);
                MetricBuilder.boradcastMeter.mark(t0);
            }
        }

        return true;
    }

}
