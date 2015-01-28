package com.argo.qpush.core.service;

import com.argo.qpush.core.TxMain;
import com.argo.qpush.core.entity.Payload;

import java.util.List;

/**
 * Created by user on 1/28/15.
 */
public interface PayloadService {

    Payload get(long id);

    @TxMain
    void add(Payload payload);

    @TxMain
    void saveAfterSent(Payload payload) throws Exception;

    List<Payload> findNormalList(int productId, long start, int page, int limit);

    List<Payload> findBrodcastList(int productId, long start, int page, int limit);

    @TxMain
    void updateSendStatus(Payload message, int counting);

    Payload findLatest(int productId, String userId);
}
