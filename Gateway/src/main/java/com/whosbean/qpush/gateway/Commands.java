package com.whosbean.qpush.gateway;

/**
 * Created by yaming_deng on 14-8-6.
 */
public interface Commands {

    /**
     * 上线
     */
    final int GO_ONLINE = 1;
    /**
     * 心跳维持
     */
    final int KEEP_ALIVE = 2;
    /**
     * 推送反馈
     */
    final int PUSH_ACK = 3;
    /**
     * 离线
     */
    final int GO_OFFLINE = 0;
}
