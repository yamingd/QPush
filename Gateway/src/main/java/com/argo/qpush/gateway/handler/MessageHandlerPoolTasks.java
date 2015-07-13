package com.argo.qpush.gateway.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by user on 1/22/15.
 */
@Component
public class MessageHandlerPoolTasks implements InitializingBean, DisposableBean {

    protected static Logger logger = LoggerFactory.getLogger(MessageHandlerPoolTasks.class);

    public static MessageHandlerPoolTasks instance = null;

    private ThreadPoolTaskExecutor poolTaskExecutor;

    private volatile boolean stopping;

    @Autowired
    private Properties appConfig;

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;

        stopping = false;

        int limit = Integer.parseInt(appConfig.getProperty("handler.executors", "100"));

        poolTaskExecutor = new ThreadPoolTaskExecutor();
        poolTaskExecutor.setCorePoolSize(limit/10);
        poolTaskExecutor.setMaxPoolSize(limit);
        poolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        poolTaskExecutor.afterPropertiesSet();

        // pool status
        Thread thread3 = new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stopping) {

                    logger.info("MessageHandlerPoolTasks: {}", poolTaskExecutor.getThreadPoolExecutor());

                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        thread3.start();
    }

    public ThreadPoolTaskExecutor getExecutor() {
        return poolTaskExecutor;
    }

    @Override
    public void destroy() throws Exception {
        stopping = true;
    }
}
