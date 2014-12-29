package com.argo.qpush.core.entity;

/**
 * 客户端状态
 * Created by yaming_deng on 14-8-6.
 */
public interface ClientStatus {

    /**
     * 刚注册
     */
    final int NewlyAdd = 0;
    /**
     * 3天内存活
     */
    final int Retention3 = 1;
    /**
     * 一周内存活
     */
    final int Retention7 = 2;
    /**
     * 两周内存活
     */
    final int Retention14 = 3;
    /**
     * 一个月内存活
     */
    final int Retention30 = 4;
    /**
     * 两个月内存活
     */
    final int Retention60 = 5;
    /**
     * 忠实用户
     */
    final int Stay = 8;
    /**
     * 已废弃
     */
    final int Gone = 9;
}
