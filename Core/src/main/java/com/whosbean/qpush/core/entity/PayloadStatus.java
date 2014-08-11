package com.whosbean.qpush.core.entity;

/**
 * 发送状态
 * Created by yaming_deng on 14-8-6.
 */
public interface PayloadStatus {
    /**
     * 等待发送
     */
    final int Pending = 0;
    /**
     * 已发送
     */
    final int Sent = 1;
    /**
     * 发送失败
     */
    final int Failed = 2;
}
