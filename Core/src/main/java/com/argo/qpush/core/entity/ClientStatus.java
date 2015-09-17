package com.argo.qpush.core.entity;

/**
 * 客户端状态
 * Created by yaming_deng on 14-8-6.
 */
public interface ClientStatus {

    final int Offline = 0;

    final int Online = 1;

    final int Sleep = 2;
}
