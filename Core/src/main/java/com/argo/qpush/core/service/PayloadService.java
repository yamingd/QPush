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

    /**
     * 保存一个payload记录
     * @param payload
     */
    @TxMain
    void add(Payload payload);

    /**
     * 发送后保存记录及状态
     * @param payload
     * @throws Exception
     */
    @TxMain
    void saveAfterSent(Payload payload) throws Exception;

    /**
     * 读取p2p项
     * @param productId
     * @param start
     * @param page
     * @param limit
     * @return
     */
    List<Payload> findNormalList(int productId, long start, int page, int limit);

    /**
     * 读取广播项
     * @param productId
     * @param start
     * @param page
     * @param limit
     * @return
     */
    List<Payload> findBrodcastList(int productId, long start, int page, int limit);

    /**
     * 更新发送状态
     * @param message
     * @param counting
     */
    @TxMain
    void updateSendStatus(Payload message, int counting);

    /**
     * 读取离线消息
     * @param productId
     * @param userId
     * @return
     */
    List<Long> findLatestToOfflineClients(int productId, String userId, long start);
}
