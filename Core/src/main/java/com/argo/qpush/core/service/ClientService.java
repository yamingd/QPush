package com.argo.qpush.core.service;

import com.argo.qpush.core.TxMain;
import com.argo.qpush.core.entity.Client;

import java.util.List;

/**
 * Created by user on 1/28/15.
 */
public interface ClientService {

    @TxMain
    void add(Client client);

    Client findByUserId(String userId);

    List<Client> findOfflineByType(Integer productId, Integer typeId, Integer page, Integer limit);

    int countOfflineByType(Integer productId, Integer typeId);

    @TxMain
    void updateOnlineTs(Client client);

    @TxMain
    void updateOfflineTs(long id, int lastSendTs);

    @TxMain
    void updateStatus(long id, int statusId);

    @TxMain
    void updateBadge(String userId, int count);
}
