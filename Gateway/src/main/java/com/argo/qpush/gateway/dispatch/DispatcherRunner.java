package com.argo.qpush.gateway.dispatch;

import com.google.common.collect.Lists;
import com.argo.qpush.core.entity.Product;
import com.argo.qpush.core.service.ProductService;
import com.argo.qpush.gateway.keeper.APNSKeeper;
import com.argo.qpush.gateway.keeper.ClientKeeper;
import com.argo.qpush.gateway.keeper.ConnectionKeeper;
import com.argo.qpush.pipe.PayloadQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.Lifecycle;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Properties;

/**
 * Created by yaming_deng on 14-8-8.
 */
@Component
public class DispatcherRunner implements InitializingBean, ApplicationContextAware, Lifecycle {

    protected static Logger logger = LoggerFactory.getLogger(DispatcherRunner.class);

    public static DispatcherRunner instance;

    @Autowired
    @Qualifier("appConfig")
    private Properties serverConfig;

    @Autowired
    private ProductService productService;

    private PayloadQueue payloadQueue;

    private List<Product> productList;

    private List<Dispatcher> dispatcherList;

    private ApplicationContext applicationContext;

    private volatile boolean running = false;

    @Override
    public synchronized void start() {
        if (running){
            logger.info("Dispatcher Runner is running.");
            return;
        }

        logger.info("Dispatcher Runner starting.");
        running = true;
        ConnectionKeeper.init();
        productList = productService.findAll();
        String beanName = this.serverConfig.getProperty("payloadQueue", "default");
        logger.info("USING PayloadQueue = " + beanName);
        this.payloadQueue = (PayloadQueue) this.applicationContext.getBean(beanName);
        this.payloadQueue.init();
        this.initDispatcher(this.productList);
        new DispatcherRefreshThread().start();
    }

    @Override
    public synchronized void stop() {
        running = false;
        for (Dispatcher dispatcher : dispatcherList){
            dispatcher.stopDispatch();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    public class DispatcherRefreshThread extends Thread{

        public void run(){

            int min = Integer.parseInt(serverConfig.getProperty(Dispatcher.DISPATCHER_INTERVAL, "1000"));

            while (running){

                int id = productList.size();

                if (id > 0){

                    id = productList.get(id - 1).getId();

                    List<Product> news = productService.findNewest(id);
                    if (news.size() > 0){
                        logger.info("Found new Product. total=" + news.size());
                        initDispatcher(news);
                        productList.addAll(news);
                    }
                }

                try {
                    Thread.sleep(min);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private void initDispatcher(List<Product> prods){
        for (Product prod : prods){
            //注册产品
            ClientKeeper.registry(prod.getAppKey());
            APNSKeeper.get(prod);
            //启动推送器
            Dispatcher dispatcher = new Dispatcher(serverConfig, prod, payloadQueue);
            dispatcherList.add(dispatcher);
            dispatcher.start();
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        dispatcherList = Lists.newArrayList();
        this.start();
        instance = this;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public Dispatcher get(String appKey){
        for (Dispatcher item : dispatcherList){
            if (item.getProduct().getAppKey().equalsIgnoreCase(appKey)){
                return item;
            }
        }
        return null;
    }
}
