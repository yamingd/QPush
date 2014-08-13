package com.whosbean.qpush.gateway;

import java.util.Properties;

/**
 * Created by yaming_deng on 14-8-13.
 */
public class ServerConfig {

    private static Properties conf;

    public static Properties getConf() {
        return conf;
    }

    public static void setConf(Properties conf) {
        ServerConfig.conf = conf;
    }
}
