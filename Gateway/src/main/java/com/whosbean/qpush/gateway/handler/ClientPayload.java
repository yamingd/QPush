package com.whosbean.qpush.gateway.handler;

/**
 * Created by yaming_deng on 14-8-6.
 */
public class ClientPayload {

    /**
     * 操作
     */
    private Integer cmd;
    /**
     * 用户设备APNS令牌
     */
    private String token;
    /**
     * 应用唯一标示
     */
    private String appKey;
    /**
     * 用户id
     */
    private String userId;
    /**
     * 设备类型. 参考ClientType枚举定义.
     */
    private Integer typeId;

    public Integer getCmd() {
        return cmd;
    }

    public void setCmd(Integer cmd) {
        this.cmd = cmd;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getTypeId() {
        return typeId;
    }

    /**
     * @see com.whosbean.qpush.core.entity.ClientType
     * @param typeId
     */
    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }
}
