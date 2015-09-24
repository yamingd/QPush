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

    private boolean sandBox  = true;

    @Override
    public void afterPropertiesSet() throws Exception {
        current = this;

        Object flag = this.serverConfig.get("apns.sandbox");
        if (flag != null && !((Boolean)flag)) {
            this.sandBox = false;
        }else{
            this.sandBox = true;
        }
    }

    public Properties get() {
        return serverConfig;
    }

    public boolean isSandBox() {
        return sandBox;
    }
}
