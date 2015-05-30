package com.argo.qpush.publisher.handler;

import com.argo.qpush.client.PayloadMessage;
import com.argo.qpush.core.entity.Payload;
import com.argo.qpush.core.entity.Product;
import com.argo.qpush.core.service.ProductService;
import com.argo.qpush.pipe.PayloadQueue;
import com.google.common.collect.Maps;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by yaming_deng on 2014/9/25.
 */
@Component
public class PayloadHandler implements InitializingBean, ApplicationContextAware {

    protected static Logger logger = LoggerFactory.getLogger(PayloadHandler.class);

    private ApplicationContext applicationContext;
    public static PayloadHandler instance = null;

    @Autowired
    @Qualifier("payloadMysqlQueue")
    private PayloadQueue defaultQueue;

    @Autowired
    @Qualifier("appConfig")
    private Properties conf;

    @Autowired
    private ProductService productService;

    private ConcurrentMap<String, Integer> productIdMaps = Maps.newConcurrentMap();

    private PayloadQueue queue;

    @Override
    public void afterPropertiesSet() throws Exception {

        String beanName = conf.getProperty("payloadQueue", "payloadMysqlQueue");
        queue = this.applicationContext.getBean(beanName, PayloadQueue.class);

        instance = this;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * 保存Payload到 Queue.
     * Queue 根据配置项payloadQueue决定
     *
     * @param message
     */
    public void save(PayloadMessage message) {
        Integer pid = productIdMaps.get(message.appkey);
        if (null == pid) {
            Product product = productService.findByKey(message.appkey);
            if (product == null){
                logger.error("Product not found. appkey=" + message.appkey);
                return;
            }
            pid = product.getId();
            productIdMaps.put(message.appkey, pid);
        }

        Payload payload = new Payload(message);
        payload.setProductId(pid);
        queue.add(payload);

    }
}
