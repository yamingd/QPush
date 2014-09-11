package com.whosbean.qpush.core.entity;

/**
 * 发送状态
 * Created by yaming_deng on 14-8-6.
 */
public interface PayloadStatus {
    /**
     * 等待发送并且没存入MySQL.
     */
    final int Pending0 = -1;
    /**
     * 等待发送
     */
    final int Pending = 1;
    /**
     * 已发送
     */
    final int Sent = 2;
    /**
     * 发送失败
     */
    final int Failed = 3;
}
