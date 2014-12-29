package com.argo.qpush.apns;

import org.msgpack.annotation.Message;

/**
 * Created by yaming_deng on 14-8-6.
 */
@Message
public class APNSEvent {
    /**
     * 操作
     */
    public Integer op;
    /**
     * 用户设备APNS令牌
     */
    public String token;
    /**
     * 应用唯一标示
     */
    public String appKey;
    /**
     * 用户id
     */
    public String userId;
    /**
     * 设备类型. 参考ClientType枚举定义.
     */
    public Integer typeId;

    @Override
    public String toString() {
        return "APNSEvent{" +
                "op=" + op +
                ", token='" + token + '\'' +
                ", appKey='" + appKey + '\'' +
                ", userId='" + userId + '\'' +
                ", typeId=" + typeId +
                '}';
    }
}
