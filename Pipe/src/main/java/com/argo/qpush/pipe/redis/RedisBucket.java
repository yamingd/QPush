package com.argo.qpush.pipe.redis;

import com.google.common.collect.Lists;
import org.apache.commons.pool.BasePoolableObjectFactory;
import org.springframework.beans.factory.InitializingBean;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Pool;

import java.util.List;
import java.util.Properties;
import java.util.regex.Pattern;

/**
 * Created by yaming_deng on 14-9-5.
 */
public class RedisBucket extends Pool<BinaryShardedJedis> implements InitializingBean {

    private Properties jedisConfig;

    private JedisPoolConfig jedisPoolConfig;
    private JedisShardInfo jedisShardInfo;

    public RedisBucket(Properties jedisConfig){
        this.jedisConfig = jedisConfig;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxActive(Integer.parseInt(jedisConfig.getProperty("maxActive", "50")));
        jedisPoolConfig.setMaxIdle(Integer.parseInt(jedisConfig.getProperty("maxIdle", "10")));
        jedisPoolConfig.setMaxWait(Integer.parseInt(jedisConfig.getProperty("maxWait", "5000")));

        String host = jedisConfig.getProperty("host");
        int port = Integer.parseInt(jedisConfig.getProperty("port", "6379"));
        jedisShardInfo = new JedisShardInfo(host, port);

        List<JedisShardInfo> shards = Lists.newArrayList();
        shards.add(jedisShardInfo);

        this.initPool(jedisPoolConfig, new ShardedJedisFactory(shards, Hashing.MURMUR_HASH, null));
    }

    /**
     * PoolableObjectFactory custom impl.
     */
    private static class ShardedJedisFactory extends BasePoolableObjectFactory {
        private List<JedisShardInfo> shards;
        private Hashing algo;
        private Pattern keyTagPattern;

        public ShardedJedisFactory(List<JedisShardInfo> shards, Hashing algo,
                                   Pattern keyTagPattern) {
            this.shards = shards;
            this.algo = algo;
            this.keyTagPattern = keyTagPattern;
        }

        public Object makeObject() throws Exception {
            BinaryShardedJedis jedis = new BinaryShardedJedis(shards, algo, keyTagPattern);
            return jedis;
        }

        public void destroyObject(final Object obj) throws Exception {
            if ((obj != null) && (obj instanceof ShardedJedis)) {
                ShardedJedis shardedJedis = (ShardedJedis) obj;
                for (Jedis jedis : shardedJedis.getAllShards()) {
                    try {
                        try {
                            jedis.quit();
                        } catch (Exception e) {

                        }
                        jedis.disconnect();
                    } catch (Exception e) {

                    }
                }
            }
        }

        public boolean validateObject(final Object obj) {
            try {
                ShardedJedis jedis = (ShardedJedis) obj;
                for (Jedis shard : jedis.getAllShards()) {
                    if (!shard.ping().equals("PONG")) {
                        return false;
                    }
                }
                return true;
            } catch (Exception ex) {
                return false;
            }
        }
    }

}
