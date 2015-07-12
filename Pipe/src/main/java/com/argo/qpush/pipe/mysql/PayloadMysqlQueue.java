package com.argo.qpush.pipe.mysql;

import com.argo.qpush.core.entity.Payload;
import com.argo.qpush.core.service.PayloadService;
import com.argo.qpush.pipe.PayloadCursor;
import com.argo.qpush.pipe.PayloadQueue;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 * 使用Mysql数据库存储.
 *
 * Created by yaming_deng on 14-8-8.
 */
@Deprecated
@Component("payloadMysqlQueue")
public class PayloadMysqlQueue implements PayloadQueue {

    @Autowired
    private PayloadService payloadService;

    @Override
    public void init() {

    }

    @Override
    public List<Payload> getNormalItems(PayloadCursor cursor) {
        return this.payloadService.findNormalList(cursor.getProduct().getId(), cursor.getStartId(), cursor.getPage(), cursor.getLimit());
    }

    @Override
    public List<Payload> getBroadcastItems(PayloadCursor cursor) {
        return this.payloadService.findBrodcastList(cursor.getProduct().getId(), cursor.getStartId(), cursor.getPage(), cursor.getLimit());
    }

    @Override
    public void add(Payload payload) {
        payloadService.add(payload);
    }
}
