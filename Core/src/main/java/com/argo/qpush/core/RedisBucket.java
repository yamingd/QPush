package com.argo.qpush.core;

import org.springframework.beans.factory.InitializingBean;
import redis.clients.jedis.BinaryJedis;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.util.Pool;

import java.util.Properties;

/**
 * Created by yaming_deng on 14-9-5.
 */
public class RedisBucket extends Pool<BinaryJedis> implements InitializingBean {

    private Properties jedisConfig;

    private JedisPoolConfig jedisPoolConfig;
    private JedisShardInfo jedisShardInfo;

    public RedisBucket(Properties jedisConfig){
        this.jedisConfig = jedisConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(Integer.parseInt(jedisConfig.getProperty("maxActive", "50")));
        jedisPoolConfig.setMaxIdle(Integer.parseInt(jedisConfig.getProperty("maxIdle", "10")));
        jedisPoolConfig.setMaxWaitMillis(Integer.parseInt(jedisConfig.getProperty("maxWait", "5000")));

        String host = jedisConfig.getProperty("host");
        int port = Integer.parseInt(jedisConfig.getProperty("port", "6379"));
        BinaryJedisFactory factory = new BinaryJedisFactory(host, port, 2000, null, 0);
        this.initPool(jedisPoolConfig, factory);
    }

}
