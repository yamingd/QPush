package com.whosbean.qpush.pipe.redis;

import org.apache.commons.pool.BasePoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import redis.clients.jedis.BinaryShardedJedis;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisShardInfo;
import redis.clients.jedis.ShardedJedis;
import redis.clients.util.Hashing;
import redis.clients.util.Pool;

import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by yaming_deng on 14-9-5.
 */
public class RedisBucket extends Pool<BinaryShardedJedis> {

    public RedisBucket(final GenericObjectPool.Config poolConfig,
                            List<JedisShardInfo> shards) {
        this(poolConfig, shards, Hashing.MURMUR_HASH);
    }

    public RedisBucket(final GenericObjectPool.Config poolConfig,
                            List<JedisShardInfo> shards, Hashing algo) {
        this(poolConfig, shards, algo, null);
    }

    public RedisBucket(final GenericObjectPool.Config poolConfig,
                            List<JedisShardInfo> shards, Pattern keyTagPattern) {
        this(poolConfig, shards, Hashing.MURMUR_HASH, keyTagPattern);
    }

    public RedisBucket(final GenericObjectPool.Config poolConfig,
                            List<JedisShardInfo> shards, Hashing algo, Pattern keyTagPattern) {
        super(poolConfig, new ShardedJedisFactory(shards, algo, keyTagPattern));
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
