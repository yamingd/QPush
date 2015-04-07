package com.argo.qpush.core.service;

import com.argo.qpush.core.TxMain;
import com.argo.qpush.core.entity.Payload;

import java.util.List;

/**
 * Created by user on 1/28/15.
 */
public interface PayloadService {

    /**
     * 读取详细. 仅是payload
     * @param id
     * @return
     */
    Payload getSimple(long id);
    List<Payload> getSimpleList(List<Long> ids);
    /**
     * 读取详细，包括clients
     * @param id
     * @return
     */
    Payload get(long id);

    @TxMain
    void add(Payload payload);

    @TxMain
    void saveAfterSent(Payload payload) throws Exception;

    List<Payload> findNormalList(int productId, long start, int page, int limit);

    List<Payload> findBrodcastList(int productId, long start, int page, int limit);

    @TxMain
    void updateSendStatus(Payload message, int counting);

    /**
     * 读取离线消息
     * @param productId
     * @param userId
     * @return
     */
    List<Long> findLatest(int productId, String userId, long start);
}
