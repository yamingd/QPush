package com.argo.qpush.gateway.dispatch;

import com.argo.qpush.core.entity.Payload;
import com.argo.qpush.core.entity.Product;
import com.argo.qpush.gateway.Connection;
import com.argo.qpush.gateway.SentProgress;
import com.argo.qpush.gateway.keeper.ClientKeeper;
import com.argo.qpush.gateway.keeper.ConnectionKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * 广播推送.
 * Created by yaming_deng on 14-8-8.
 */
public class BroadcastThread implements Callable<Integer> {

    protected static Logger logger = LoggerFactory.getLogger(BroadcastThread.class);

    private Payload message;
    private int start = 0;
    private int limit = 100;
    private Product product;
    private SentProgress progress;

    public BroadcastThread(final Product product,
                           final Payload message, int start, int limit,
                           final SentProgress progress) {
        super();
        this.message = message;
        this.start = start;
        this.limit = limit;
        this.product = product;
        this.progress = progress;
    }

    @Override
    public Integer call() throws Exception {
        if(message == null){
            return 0;
        }
        if(message.getClients() == null || message.getClients().size() == 0){
            Collection<Integer> cids = ClientKeeper.gets(product.getKey());
            int s = start * limit;
            if(cids.size() < limit){
                limit = cids.size();
            }
            Integer[] temp = cids.toArray(new Integer[0]);
            //SentProgress progress = new SentProgress(limit);
            for(int i=0; i<limit; i++){
                int cid = temp[s+i];
                Connection c = ConnectionKeeper.get(cid);
                if(c!=null){
                    c.send(progress, message);
                }else{
                    progress.incrFailed();
                }
            }

            return 1;
        }

        return 0;
    }

}
