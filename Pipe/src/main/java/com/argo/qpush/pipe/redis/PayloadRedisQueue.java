package com.argo.qpush.pipe.redis;

import com.argo.qpush.core.MessageUtils;
import com.argo.qpush.core.RedisBucket;
import com.argo.qpush.core.entity.Payload;
import com.argo.qpush.core.entity.PayloadStatus;
import com.argo.qpush.core.service.PayloadService;
import com.argo.qpush.pipe.PayloadCursor;
import com.argo.qpush.pipe.PayloadQueue;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.BinaryJedis;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Created by yaming_deng on 14-8-11.
 */
public class PayloadRedisQueue implements PayloadQueue, InitializingBean {

    public static final byte[] QPUSH_PENDING = "qpush:pending".getBytes();
    public static final byte[] QPUSH_PK = "pk:payload".getBytes();

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private PayloadService payloadService;

    @Autowired
    private RedisBucket redisBucket;

    private List<Payload> emptyList = Lists.newArrayList();

    @Override
    public void init() {

    }

    @Override
    public List<Payload> getNormalItems(PayloadCursor cursor) {
        BinaryJedis jedis =  redisBucket.getResource();
        try {
            byte[] key = String.format("qpush:{%s:%s}.q", cursor.getProduct().getId(), 0).getBytes();
            List<Payload> ids = Lists.newArrayList();
            for (int i = 0; i < cursor.getLimit(); i++) {
                byte[] t = jedis.lpop(key);
                if (t==null || t.length == 0){
                    break;
                }
                try {
                    Payload item = MessageUtils.asT(Payload.class, t);
                    ids.add(item);
                } catch (IOException e) {
                    logger.error("解析消息错误", e);
                }
            }
            if (ids.size() > 0){
                jedis.decrBy(QPUSH_PENDING, ids.size());
            }
            redisBucket.returnResource(jedis);
            return ids;
        } catch (Exception e) {
            logger.error("读取消息错误", e);
            redisBucket.returnBrokenResource(jedis);
        }

        return emptyList;
    }

    @Override
    public List<Payload> getBroadcastItems(PayloadCursor cursor) {
        BinaryJedis jedis =  redisBucket.getResource();
        try {
            byte[] key = String.format("qpush:{%s:%s}.q", cursor.getProduct().getId(), 1).getBytes();
            List<Payload> ids = Lists.newArrayList();
            for (int i = 0; i < cursor.getLimit(); i++) {
                byte[] t = jedis.lpop(key);
                if (t == null || t.length == 0){
                    break;
                }
                Payload item = MessageUtils.asT(Payload.class, t);
                ids.add(item);
            }
            if (ids.size() > 0){
                jedis.decrBy(QPUSH_PENDING, ids.size());
            }
            redisBucket.returnResource(jedis);
            return ids;
        } catch (Exception e) {
            logger.error("读取消息错误", e);
            redisBucket.returnBrokenResource(jedis);
        }

        return emptyList;
    }

    @Override
    public void add(Payload payload) {
        BinaryJedis jedis =  redisBucket.getResource();
        try {
            long id = jedis.incr(QPUSH_PK);
            payload.setId(id);
            payload.setStatusId(PayloadStatus.Pending0);
            payload.setCreateAt(new Date().getTime()/1000);
            String key = String.format("qpush:{%s:%s}.q", payload.getProductId(), payload.getBroadcast());
            long ret = jedis.rpush(key.getBytes(), MessageUtils.asBytes(payload));
            long total = jedis.incr(QPUSH_PENDING);
            redisBucket.returnResource(jedis);
            logger.info("qpush.pending total = {}, pushRet={}", total, ret);
        } catch (Exception e) {
            logger.error("添加消息进Redis错误", e);
            redisBucket.returnBrokenResource(jedis);
        }

    }


    @Override
    public void afterPropertiesSet() throws Exception {
        init();
    }
}
