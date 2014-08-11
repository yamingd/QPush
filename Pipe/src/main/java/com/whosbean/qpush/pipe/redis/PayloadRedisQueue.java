package com.whosbean.qpush.pipe.redis;

import com.google.common.collect.Lists;
import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.core.service.PayloadService;
import com.whosbean.qpush.pipe.PayloadCursor;
import com.whosbean.qpush.pipe.PayloadQueue;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.ShardedJedis;
import redis.clients.jedis.ShardedJedisPool;

import java.util.List;

/**
 * Created by yaming_deng on 14-8-11.
 */
@Component("payloadRedisQueue")
public class PayloadRedisQueue implements PayloadQueue {

    public static final String QPUSH_PENDING = "qpush:pending";
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PayloadService payloadService;

    @Autowired
    private ShardedJedisPool shardedJedisPool;

    private List<Long> emptyList = Lists.newArrayList();

    @Override
    public void init() {

    }

    @Override
    public List<Long> getNormalItems(PayloadCursor cursor) {
        ShardedJedis jedis =  shardedJedisPool.getResource();
        try {
            String key = String.format("qpush:{%s:%s}.q", cursor.getProduct().getId(), 0);
            List<Long> ids = Lists.newArrayList();
            for (int i = 0; i < cursor.getLimit(); i++) {
                String t = jedis.lpop(key);
                if (StringUtils.isBlank(t)){
                    break;
                }
                ids.add(Long.parseLong(t));
            }
            if (ids.size() > 0){
                jedis.decrBy(QPUSH_PENDING, ids.size());
            }
            shardedJedisPool.returnResource(jedis);
            return ids;
        } catch (Exception e) {
            logger.error("添加消息进Redis错误", e);
            shardedJedisPool.returnBrokenResource(jedis);
        }

        return emptyList;
    }

    @Override
    public List<Long> getBroadcastItems(PayloadCursor cursor) {
        ShardedJedis jedis =  shardedJedisPool.getResource();
        try {
            String key = String.format("qpush:{%s:%s}.q", cursor.getProduct().getId(), 1);
            List<Long> ids = Lists.newArrayList();
            for (int i = 0; i < cursor.getLimit(); i++) {
                String t = jedis.lpop(key);
                if (StringUtils.isBlank(t)){
                    break;
                }
                ids.add(Long.parseLong(t));
            }
            if (ids.size() > 0){
                jedis.decrBy(QPUSH_PENDING, ids.size());
            }
            shardedJedisPool.returnResource(jedis);
            return ids;
        } catch (Exception e) {
            logger.error("添加消息进Redis错误", e);
            shardedJedisPool.returnBrokenResource(jedis);
        }

        return emptyList;
    }

    @Override
    public void add(Payload payload) {
        PayloadService.instance.add(payload);

        ShardedJedis jedis =  shardedJedisPool.getResource();
        try {
            String key = String.format("qpush:{%s:%s}.q", payload.getProductId(), payload.getBroadcast());
            jedis.rpush(key, String.valueOf(payload.getId()));
            long total = jedis.incr(QPUSH_PENDING);
            shardedJedisPool.returnResource(jedis);
            logger.info("qpush.pending total = " + total);
        } catch (Exception e) {
            logger.error("添加消息进Redis错误", e);
            shardedJedisPool.returnBrokenResource(jedis);
        }
    }


}
