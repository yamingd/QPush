package com.whosbean.qpush.core.entity;

import java.util.Date;

/**
 * 移动客户端设备
 * Created by yaming_deng on 14-8-6.
 */
public class Client {

    private Long id;
    private Integer productId;
    private String userId;
    private String deviceToken;
    private Date createAt;
    private Integer statusId;
    private Integer typeId;
    private Date lastSendAt;
    private Date lastOnline;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getProductId() {
        return productId;
    }

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

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Integer getTypeId() {
        return typeId;
    }

    public void setTypeId(Integer typeId) {
        this.typeId = typeId;
    }

    public Date getLastSendAt() {
        return lastSendAt;
    }

    public void setLastSendAt(Date lastSendAt) {
        this.lastSendAt = lastSendAt;
    }

    public Date getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(Date lastOnline) {
        this.lastOnline = lastOnline;
    }
}
