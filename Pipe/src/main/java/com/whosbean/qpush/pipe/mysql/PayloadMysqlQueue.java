package com.whosbean.qpush.pipe.mysql;

import com.whosbean.qpush.core.entity.Payload;
import com.whosbean.qpush.core.service.PayloadService;
import com.whosbean.qpush.pipe.PayloadCursor;
import com.whosbean.qpush.pipe.PayloadQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * 使用Mysql数据库存储.
 *
 * Created by yaming_deng on 14-8-8.
 */
@Component("payloadMysqlQueue")
public class PayloadMysqlQueue implements PayloadQueue {

    @Autowired
    private PayloadService payloadService;

    @Override
    public void init() {

    }

    @Override
    public List<Long> getNormalItems(PayloadCursor cursor) {
        return this.payloadService.findNormalList(cursor.getProduct().getId(), cursor.getStartId(), cursor.getPage(), cursor.getLimit());
    }

    @Override
    public List<Long> getBroadcastItems(PayloadCursor cursor) {
        return this.payloadService.findBrodcastList(cursor.getProduct().getId(), cursor.getStartId(), cursor.getPage(), cursor.getLimit());
    }

    @Override
    public void add(Payload payload) {
        PayloadService.instance.add(payload);
    }
}
