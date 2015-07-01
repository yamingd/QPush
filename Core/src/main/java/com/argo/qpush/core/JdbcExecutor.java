package com.argo.qpush.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by yamingd on 7/1/15.
 */
@Component
public class JdbcExecutor implements InitializingBean, DisposableBean {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    public static JdbcExecutor instance;

    @Autowired
    @Qualifier("appConfig")
    protected Properties appConfigs;

    private ThreadPoolTaskExecutor jdbcExecutor = null;

    private volatile boolean stopping = false;

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;

        int limit = Integer.parseInt(appConfigs.getProperty("jdbc.executors", "100"));

        //实际扫描线程池
        jdbcExecutor = new ThreadPoolTaskExecutor();
        jdbcExecutor.setCorePoolSize(limit/5);
        jdbcExecutor.setMaxPoolSize(limit);
        jdbcExecutor.setWaitForTasksToCompleteOnShutdown(true);
        jdbcExecutor.afterPropertiesSet();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stopping) {

                    logger.info("JdbcExecutor Status\n. {}", jdbcExecutor.getThreadPoolExecutor());

                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }

    public void submit(Runnable runnable){
        this.jdbcExecutor.submit(runnable);
    }


    @Override
    public void destroy() throws Exception {
        stopping = true;
    }
}
