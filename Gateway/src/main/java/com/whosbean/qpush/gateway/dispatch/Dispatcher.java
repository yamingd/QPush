package com.whosbean.qpush.gateway.dispatch;

import com.whosbean.qpush.core.entity.ClientType;
import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.core.entity.Product;
import com.whosbean.qpush.core.service.ClientService;
import com.whosbean.qpush.gateway.keeper.ClientKeeper;
import com.whosbean.qpush.pipe.PayloadCursor;
import com.whosbean.qpush.pipe.PayloadQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 *
 * 每个产品一个Dispatcher
 *
 * Created by yaming_deng on 14-8-8.
 */
public class Dispatcher extends Thread {

    public static final String DISPATCHER_INTERVAL = "dispatcher.interval";
    public static final String DISPATCHER_THREAD_MIN = "dispatcher.thread_min";
    public static final String DISPATCHER_THREAD_MAX = "dispatcher.thread_max";
    public static final String DISPATCHER_BROADCAST_LIMIT = "dispatcher.broadcast_limit";

    protected static Logger logger = LoggerFactory.getLogger(Dispatcher.class);

    private Properties conf;
    private PayloadQueue queue;
    private Product product;

    private PayloadCursor singleCursor;
    private PayloadCursor broadcastCursor;

    private ThreadPoolTaskExecutor singlePool;
    private ThreadPoolTaskExecutor broadcastPool;

    private volatile boolean stopping;

    public Dispatcher(Properties conf, Product product, PayloadQueue queue) {
        this.conf = conf;
        this.queue = queue;
        this.product = product;
        this.singleCursor = new PayloadCursor(product);
        this.broadcastCursor = new PayloadCursor(product);
        this.singlePool = this.createPool();
        this.broadcastPool = this.createPool();
        this.stopping = false;
    }

    protected ThreadPoolTaskExecutor createPool() {
        int min = Integer.parseInt(this.conf.getProperty(DISPATCHER_THREAD_MIN, "10"));
        int max = Integer.parseInt(this.conf.getProperty(DISPATCHER_THREAD_MAX, "100"));

        ThreadPoolTaskExecutor exe = new ThreadPoolTaskExecutor();
        exe.setCorePoolSize(min);
        exe.setMaxPoolSize(max);
        exe.setWaitForTasksToCompleteOnShutdown(true);
        exe.afterPropertiesSet();

        return exe;
    }

    @Override
    public void run(){

        logger.info("Dispatcher start to run. " + this.product);

        int min = Integer.parseInt(this.conf.getProperty(DISPATCHER_INTERVAL, "1"));

        while (!this.stopping) {

            int total = ClientKeeper.count(product.getAppKey());
            if(total > 0) {
                doSinglePush();
                doBroadcastPush();
            }

            try {
                Thread.sleep(min * 1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        logger.info("Dispatcher stop running. " + this.product);
    }

    public void stopDispatch(){
        this.stopping = true;
    }

    public void startDispatch(){
        this.stopping = false;
    }

    public void pushOfflinePayload(String userId){
        this.singlePool.submit(new OfflineSendThread(this.product, userId));
    }

    protected void doBroadcastPush() {
        List<Payload> items = queue.getBroadcastItems(this.broadcastCursor);
        for(Payload id : items){
            this.doBoradcast(id);
            if (this.product.getClientTypeid().intValue() == ClientType.iOS) {
                this.doBoradcastIOS(id);
            }
        }
        if (items.size() > 0){
            int size = items.size();
            broadcastCursor.setStartId(items.get(size - 1).getId());
            broadcastCursor.setTs(new Date());
        }else{
            logger.info("Dispatcher Broadcast, " + product + ", total = " + items.size());
        }
    }

    protected void doSinglePush() {
        List<Payload> items = queue.getNormalItems(this.singleCursor);
        for(Payload id : items){
            this.singlePool.submit(new OneSendThread(this.product, id));
        }
        if (items.size() > 0){
            int size = items.size();
            singleCursor.setStartId(items.get(size - 1).getId());
            singleCursor.setTs(new Date());
        }else{
            logger.info("Dispatcher Single, " + product + ", total = " + items.size());
        }
    }

    protected void doBoradcast(Payload message){
        int total = ClientKeeper.count(this.product.getAppKey());
        if (total == 0){
            logger.info("Dispatcher Broadcast, " + product + ", total Client = " + total);
            return;
        }
        //每个线程发送100个客户端.
        int limit = Integer.parseInt(this.conf.getProperty(DISPATCHER_BROADCAST_LIMIT, "100"));
        int pages = total / limit;
        if(total % limit > 0){
            pages ++;
        }
        for(int i=0; i<pages; i++){
            this.broadcastPool.submit(new BroadcastThread(this.product, message, i, limit));
        }
    }

    protected void doBoradcastIOS(Payload message){
        long total = ClientService.instance.countOfflineByType(this.product.getId(), ClientType.iOS);
        if (total == 0){
            logger.info("Dispatcher Broadcast, " + product + ", total Client = " + total);
            return;
        }
        //每个线程发送100个客户端.
        int limit = Integer.parseInt(this.conf.getProperty(DISPATCHER_BROADCAST_LIMIT, "100"));
        long pages = total / limit;
        if(total % limit > 0){
            pages ++;
        }
        for(int i=0; i<pages; i++){
            this.broadcastPool.submit(new BroadcastIOSThread(this.product, message, i, limit));
        }
    }

    public Product getProduct() {
        return product;
    }
}
