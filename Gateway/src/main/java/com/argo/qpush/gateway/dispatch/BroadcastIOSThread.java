package com.argo.qpush.gateway.dispatch;

import com.argo.qpush.core.entity.Client;
import com.argo.qpush.core.entity.ClientType;
import com.argo.qpush.core.entity.Payload;
import com.argo.qpush.core.entity.Product;
import com.argo.qpush.core.service.ClientServiceImpl;
import com.argo.qpush.gateway.SentProgress;
import com.argo.qpush.gateway.keeper.APNSKeeper;
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
    private SentProgress progress;

    public BroadcastIOSThread(final Product product,
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
            progress.incrFailed();
            return 0;
        }
        if(message.getClients() == null || message.getClients().size() == 0){
            List<Client> clients = ClientServiceImpl.instance.findOfflineByType(this.product.getId(), ClientType.iOS, this.start, this.limit);
            for (Client c : clients){
                APNSKeeper.instance.push(this.product, c, message);
            }
            return 1;
        }else{
            progress.incrFailed();
        }

        return 0;
    }
}
