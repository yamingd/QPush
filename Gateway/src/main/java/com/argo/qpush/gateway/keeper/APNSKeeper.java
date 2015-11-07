package com.argo.qpush.gateway.keeper;

import com.argo.qpush.core.entity.Client;
import com.argo.qpush.core.entity.Payload;
import com.argo.qpush.core.entity.Product;
import com.argo.qpush.core.entity.PushStatus;
import com.argo.qpush.core.service.ClientServiceImpl;
import com.argo.qpush.core.service.ProductService;
import com.google.common.collect.Maps;
import com.relayrides.pushy.apns.*;
import com.relayrides.pushy.apns.util.MalformedTokenStringException;
import com.relayrides.pushy.apns.util.SSLContextUtil;
import com.relayrides.pushy.apns.util.SimpleApnsPushNotification;
import com.relayrides.pushy.apns.util.TokenUtil;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLHandshakeException;
import java.io.IOException;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by yaming_deng on 14-8-13.
 */
@Component
public class APNSKeeper implements InitializingBean {

    protected static Logger logger = LoggerFactory.getLogger(APNSKeeper.class);

    private static Map<Integer, PushManager> mapping = Maps.newConcurrentMap();


    @Autowired
    @Qualifier("appConfig")
    private Properties serverConfig;

    @Autowired
    private ProductService productService;

    private List<Product> productList;
    private boolean sandBox = true;

    class PushRejectedNotificationListener implements RejectedNotificationListener<SimpleApnsPushNotification> {

        @Override
        public void handleRejectedNotification(
                final PushManager<? extends SimpleApnsPushNotification> pushManager,
                final SimpleApnsPushNotification notification,
                final RejectedNotificationReason reason) {

            logger.error("[%s] %s was rejected with rejection reason %s\n", pushManager.getName(), notification, reason);

        }
    }

    class PushFailedConnectionListener implements FailedConnectionListener<SimpleApnsPushNotification> {

        @Override
        public void handleFailedConnection(
                final PushManager<? extends SimpleApnsPushNotification> pushManager,
                final Throwable cause) {

            logger.error(pushManager.getName() + " failed to connect Apple APNS server. ", cause);

            if (cause instanceof SSLHandshakeException) {
                // This is probably a permanent failure, and we should shut down
                // the PushManager.
            }
        }
    }


    public PushManager get(Product product){

        if (StringUtils.isBlank(product.getDevCertPath())
                || StringUtils.isBlank(product.getDevCertPass())
                || StringUtils.isBlank(product.getCertPath())
                || StringUtils.isBlank(product.getCertPass())){
            logger.error("Product iOS Push Service Miss Cert Path and Password. {}", product);
            return null;
        }

        PushManager service = mapping.get(product.getId());
        if (service == null){

            ApnsEnvironment apnsEnvironment = null;
            SSLContext sslContext = null;

            try {
                if (sandBox){
                    apnsEnvironment = ApnsEnvironment.getSandboxEnvironment();
                    sslContext = SSLContextUtil.createDefaultSSLContext(product.getDevCertPath(), product.getDevCertPass());
                }else{
                    apnsEnvironment = ApnsEnvironment.getProductionEnvironment();
                    sslContext = SSLContextUtil.createDefaultSSLContext(product.getCertPath(), product.getCertPass());
                }
            } catch (KeyStoreException e) {
                logger.error(e.getMessage(), e);
            } catch (NoSuchAlgorithmException e) {
                logger.error(e.getMessage(), e);
            } catch (CertificateException e) {
                logger.error(e.getMessage(), e);
            } catch (UnrecoverableKeyException e) {
                logger.error(e.getMessage(), e);
            } catch (KeyManagementException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }

            PushManagerConfiguration configuration = new PushManagerConfiguration();
            configuration.setConcurrentConnectionCount(1);

            final PushManager<SimpleApnsPushNotification> pushManager =
                    new PushManager<SimpleApnsPushNotification>(
                            apnsEnvironment,
                            sslContext,
                            null, // Optional: custom event loop group
                            null, // Optional: custom ExecutorService for calling listeners
                            null, // Optional: custom BlockingQueue implementation
                            configuration,
                            "ApnsPushManager-" + product.getId());

            pushManager.registerRejectedNotificationListener(new PushRejectedNotificationListener());
            pushManager.registerFailedConnectionListener(new PushFailedConnectionListener());

            pushManager.start();

//             ApnsServiceBuilder builder =  APNS.newService();
//            if (sandBox){
//                builder.withCert(product.getDevCertPath(), product.getDevCertPass());
//                builder.withSandboxDestination();
//            }else{
//                builder.withCert(product.getCertPath(), product.getCertPass());
//                builder.withProductionDestination();
//            }
//            service = builder.asPool(10).withCacheLength(Integer.MAX_VALUE).withDelegate(delegateAdapter).asQueued().build();

            mapping.put(product.getId(), pushManager);
            service = pushManager;
        }

        return service;
    }

    /**
     *
     * 使用APNS服务推送到苹果
     *
     * @param product
     * @param cc
     * @param message
     *
     */
    public void push(Product product, Client cc, Payload message){
        PushManager<SimpleApnsPushNotification> service = get(product);
        if (service != null){
            try{
                if (StringUtils.isBlank(cc.getDeviceToken()) || "NULL".equalsIgnoreCase(cc.getDeviceToken())){
                    message.setStatus(cc.getUserId(), new PushStatus(PushStatus.NO_DEVICE_TOKEN));
                }else {

                    SimpleApnsPushNotification e = wrapPayload(cc, message);
                    if (e == null){
                        message.setStatus(cc.getUserId(), new PushStatus(PushStatus.DeviceTokenInvalid));
                    }else {
                        service.getQueue().put(e);
                        message.setStatus(cc.getUserId(), new PushStatus(PushStatus.APNSSent));
                        ClientServiceImpl.instance.updateBadge(cc.getUserId(), 1);
                    }
                }
            }catch(Exception e){
                logger.error("Push Failed", e);
                message.setStatus(cc.getUserId(), new PushStatus(PushStatus.iOSPushError, e.getMessage()));
            }
        }else{
            logger.error("iOS Push Service Not Found.");
            message.setStatus(cc.getUserId(), new PushStatus(PushStatus.iOSPushConfigError));
        }
    }

    /**
     *
     * @param cc
     * @param message
     * @return
     */
    private SimpleApnsPushNotification wrapPayload(Client cc, Payload message){

        final byte[] token;
        try {
            token = TokenUtil.tokenStringToByteArray(cc.getDeviceToken());
        } catch (MalformedTokenStringException e) {
            logger.error("DeviceToken is Invalid. token=" + cc.getDeviceToken());
            return null;
        }

        Date expireDate = new Date(System.currentTimeMillis() + expireTime);
        SimpleApnsPushNotification notification = new SimpleApnsPushNotification(token, message.asJson(), expireDate);
        return notification;
    }

    /**
     * Message离线过期时间
     */
    private long expireTime = 7 * 86400 * 1000; // 一周内

    @Override
    public void afterPropertiesSet() throws Exception {
        String flag = this.serverConfig.get("apns.sandbox") + "";
        if (flag.equalsIgnoreCase("true")) {
            this.sandBox = true;
        }else{
            this.sandBox = false;
        }

        Object val = this.serverConfig.get("apns.expire");
        if (null != val){
            int days = Integer.parseInt(val + "");
            expireTime = days * 86400 * 1000;
        }

        productList = productService.findAll();
        for (Product product : productList){
            PushManager<SimpleApnsPushNotification> service = get(product);
            if (service != null) {
                logger.info("Init Product APNS service. {} / {}", product, service);
            }
        }

        instance = this;
    }

    public static APNSKeeper instance = null;
}
