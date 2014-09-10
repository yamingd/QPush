package com.whosbean.qpush.gateway.keeper;

import com.google.common.collect.Maps;
import com.notnoop.apns.APNS;
import com.notnoop.apns.ApnsService;
import com.notnoop.apns.ApnsServiceBuilder;
import com.whosbean.qpush.core.MessageUtils;
import com.whosbean.qpush.core.entity.Client;
import com.whosbean.qpush.core.entity.ClientType;
import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.core.entity.Product;
import com.whosbean.qpush.gateway.SentProgress;
import com.whosbean.qpush.gateway.ServerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by yaming_deng on 14-8-13.
 */
public class APNSKeeper {

    protected static Logger logger = LoggerFactory.getLogger(APNSKeeper.class);

    private static Map<Integer, ApnsService> mapping = Maps.newConcurrentMap();

    private static ApnsDelegateFailedAdapter delegateAdapter = new ApnsDelegateFailedAdapter();

    public static ApnsService get(Product product){

        if (product.getClientTypeid().intValue() != ClientType.iOS){
            return null;
        }

        ApnsService service = mapping.get(product.getId());
        if (service == null){
            boolean sandbox = (Boolean) ServerConfig.getConf().get("apns.sandbox");
             ApnsServiceBuilder builder =  APNS.newService();
            if (sandbox){
                builder.withCert(product.getDevCertPath(), product.getDevCertPass());
                builder.withSandboxDestination();
            }else{
                builder.withCert(product.getCertPath(), product.getCertPass());
            }
            service = builder.asPool(10).withDelegate(delegateAdapter).build();
            mapping.put(product.getId(), service);
        }
        return service;
    }

    public static void push(SentProgress progress, Product product, Client cc, Payload message){
        String json = MessageUtils.toJson(message.asAPNSMessage());
        ApnsService service = get(product);
        if (service != null){
            try{
                service.push(cc.getDeviceToken(), json);
                progress.incrSuccess();
            }
            catch(Exception e){
                logger.error("Push Failed", e);
                progress.incrFailed();
            }
        }
    }
}
