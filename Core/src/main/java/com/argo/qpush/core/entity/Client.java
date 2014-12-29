package com.argo.qpush.core.entity;

import java.util.Date;

/**
 * 移动客户端设备
 * Created by yaming_deng on 14-8-6.
 */
public class Client {

    /**
     * 客户端.
     */
    private Long id;
    /**
     * 产品id
     */
    private Integer productId;
    /**
     * 用户标示
     */
    private String userId;
    /**
     * 设备令牌
     */
    private String deviceToken;
    /**
     * 创建时间.
     */
    private Date createAt;
    /**
     * 客户端状态.
     */
    private Integer statusId;
    /**
     * 客户端类型.
     */
    private Integer typeId;
    /**
     * 最后推送时间.
     */
    private Long lastSendAt;
    /**
     * 最后在线时间
     */
    private Long lastOnline;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }

    /**
     * @see com.argo.qpush.core.entity.Product
     * @param productId
     */
    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getDeviceToken() {
        return deviceToken;
    }

    public void setDeviceToken(String deviceToken) {
        this.deviceToken = deviceToken;
    }

    public Date getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Date createAt) {
        this.createAt = createAt;
    }

    public Integer getStatusId() {
        return statusId;
    }

    /**
     * @see com.argo.qpush.core.entity.ClientStatus
     * @param statusId
     */
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Integer getTypeId() {
        return typeId;
    }

    /**
     * @see com.argo.qpush.core.entity.ClientType
     * @param typeId
     */
    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Long getLastSendAt() {
        return lastSendAt;
    }

    public void setLastSendAt(Long lastSendAt) {
        this.lastSendAt = lastSendAt;
    }

    public Long getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(Long lastOnline) {
        this.lastOnline = lastOnline;
    }

    public boolean isDevice(int typeId){
        return typeId == this.typeId.intValue();
    }
}
