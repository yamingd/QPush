package com.argo.qpush.core.entity;

/**
 * 要发送到的客户端.
 * Created by yaming_deng on 14-8-6.
 */
public class PayloadClient {

    private Long id;
    private String userId;

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
}
