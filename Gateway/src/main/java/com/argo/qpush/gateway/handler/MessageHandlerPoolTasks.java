package com.argo.qpush.gateway.handler;

import com.argo.qpush.gateway.ServerConfig;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by user on 1/22/15.
 */
@Component
public class MessageHandlerPoolTasks implements InitializingBean {

    public static MessageHandlerPoolTasks instance = null;

    private ThreadPoolTaskExecutor poolTaskExecutor;

    @Autowired
    private Properties appConfig;

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;

        int limit = Integer.parseInt(appConfig.getProperty("handler.executors", "100"));

        poolTaskExecutor = new ThreadPoolTaskExecutor();
        poolTaskExecutor.setCorePoolSize(limit/10);
        poolTaskExecutor.setMaxPoolSize(limit);
        poolTaskExecutor.setWaitForTasksToCompleteOnShutdown(true);
        poolTaskExecutor.afterPropertiesSet();
    }

    public ThreadPoolTaskExecutor getExecutor() {
        return poolTaskExecutor;
    }
}
