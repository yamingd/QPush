package com.whosbean.qpush.gateway.dispatch;

import com.whosbean.qpush.core.MetricBuilder;
import com.whosbean.qpush.core.entity.Client;
import com.whosbean.qpush.core.entity.ClientType;
import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.core.entity.Product;
import com.whosbean.qpush.core.service.ClientService;
import com.whosbean.qpush.core.service.PayloadService;
import com.whosbean.qpush.gateway.Connection;
import com.whosbean.qpush.gateway.keeper.APNSKeeper;
import com.whosbean.qpush.gateway.keeper.ConnectionKeeper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Callable;

/**
 *
 * 1对1或1对多的推送
 *
 * Created by yaming_deng on 14-8-8.
 */
public class OneSendThread implements Callable<Boolean> {

    protected static Logger logger = LoggerFactory.getLogger(OneSendThread.class);

    private Payload message;
    private Product product;

    public OneSendThread(final Product product, final Payload message) {
        super();
        this.message = message;
        this.product = product;
    }

    @Override
    public Boolean call() throws Exception {
        if(message == null){
            return true;
        }
        boolean ok = false;
        if(message.getClients()!=null){
            int total = 0;
            for (String client : message.getClients()){
                Connection c = ConnectionKeeper.get(product.getKey(), client);
                if(c != null) {
                    ok = c.send(message);
                    if(ok) {
                        total++;
                    }
                }else{
                    if (product.getClientTypeid().intValue() != ClientType.iOS){
                        continue;
                    }
                    Client cc = ClientService.instance.findByUserId(client);
                    if (cc == null){
                        logger.warn("Client not found. client=" + client);
                        continue;
                    }
                    if (!cc.isDevice(ClientType.iOS)){
                        continue;
                    }
                    if (StringUtils.isBlank(cc.getDeviceToken())){
                        logger.error("Client's deviceToken not found. client=" + client);
                        continue;
                    }
                    APNSKeeper.push(this.product, cc, message);
                    total ++;
                }
            }
            if (total > 0) {
                MetricBuilder.pushMeter.mark(total);
                MetricBuilder.pushSingleMeter.mark(total);
            }

            try {
                PayloadService.instance.updateSendStatus(message, total);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ok;
    }

}
