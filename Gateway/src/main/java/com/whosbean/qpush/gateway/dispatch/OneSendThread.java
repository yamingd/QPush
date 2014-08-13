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
            int total = 0;
            for (String client : message.getClients()){
                Connection c = ConnectionKeeper.get(product.getKey(), client);
                if(c != null) {
                    ok = c.send(message);
                    total ++;
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
                    this.pushToApple(cc, message);
                    total ++;
                }
            }
            if (total > 0) {
                MetricBuilder.pushMeter.mark(total);
                MetricBuilder.pushSingleMeter.mark(total);
            }

            try {
                PayloadService.instance.addHisotry(message, null, total, ok);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ok;
    }

    private void pushToApple(Client cc, Payload message){
        String json = GsonUtils.toJson(message.asStdMap());
        ApnsService service = APNSKeeper.get(this.product);
        if (service != null){
            service.push(cc.getDeviceToken(), json);
        }
    }
}
