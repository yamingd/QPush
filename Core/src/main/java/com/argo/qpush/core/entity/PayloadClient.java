package com.argo.qpush.core.entity;

import org.msgpack.annotation.MessagePackMessage;

/**
 * 要发送到的客户端.
 * Created by yaming_deng on 14-8-6.
 */
@MessagePackMessage
public class PayloadClient {

    private Long id;
    private String userId;
    private Long payloadId;
    private Integer statusId;
    private Integer createTime;
    private Integer tryLimit;
    private Integer errorId;
    private String errorMsg;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Long getPayloadId() {
        return payloadId;
    }

    public void setPayloadId(Long payloadId) {
        this.payloadId = payloadId;
    }

    public Integer getStatusId() {
        return statusId;
    }

    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Integer getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Integer createTime) {
        this.createTime = createTime;
    }

    public Integer getTryLimit() {
        return tryLimit;
    }

    public void setTryLimit(Integer tryLimit) {
        this.tryLimit = tryLimit;
    }

    public Integer getErrorId() {
        return errorId;
    }

    public void setErrorId(Integer errorId) {
        this.errorId = errorId;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
