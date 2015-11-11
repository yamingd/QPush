package com.argo.qpush.core.service;

import com.argo.qpush.core.TxMain;
import com.argo.qpush.core.entity.Payload;
import com.argo.qpush.core.entity.PushStatus;

import java.util.List;

/**
 * Created by user on 1/28/15.
 */
public interface PayloadService {

    /**
     * 读取详细. 仅是payload
     * @param id
     * @return Payload
     */
    Payload getSimple(long id);

    /**
     *
     * @param ids
     * @return List
     */
    List<Payload> getSimpleList(List<Long> ids);
    /**
     * 读取详细，包括clients
     * @param id
     * @return Payload
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
     * @return List
     */
    List<Payload> findNormalList(int productId, long start, int page, int limit);

    /**
     * 读取广播项
     * @param productId
     * @param start
     * @param page
     * @param limit
     * @return List
     */
    List<Payload> findBrodcastList(int productId, long start, int page, int limit);

    /**
     * 更新发送状态
     * @param message
     */
    @TxMain
    void updateSendStatus(Payload message);

    /**
     *
     * @param message
     * @param userId
     * @param pushStatus
     */
    void updateSendStatus(Payload message, String userId, PushStatus pushStatus);

    @TxMain
    void updateSendStatus(Long payloadId, String userId, PushStatus error);

    /**
     * 读取离线消息
     * @param productId
     * @param userId
     * @return List
     */
    List<Long> findLatestToOfflineClients(int productId, String userId, long start);
}
