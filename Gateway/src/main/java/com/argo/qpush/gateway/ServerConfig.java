package com.argo.qpush.gateway;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * Created by yaming_deng on 14-8-13.
 */
@Component
public class ServerConfig implements InitializingBean {

    @Autowired
    @Qualifier("appConfig")
    private Properties serverConfig;

    public static ServerConfig current;

    @Override
    public void afterPropertiesSet() throws Exception {
        current = this;
    }

    public Properties get() {
        return serverConfig;
    }

}
