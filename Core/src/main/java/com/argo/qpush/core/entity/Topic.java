package com.argo.qpush.core.entity;

import com.google.common.base.Objects;
import org.msgpack.annotation.MessagePackMessage;

import java.io.Serializable;

/**
 * Created by yamingd on 8/6/15.
 */
@MessagePackMessage
public class Topic implements Serializable {

    private Integer id;
    private String title;
    private Integer productId;
    private int status;
    private int totalClient;
    private Integer addAt;
    private Long objectId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getTotalClient() {
        return totalClient;
    }

    public void setTotalClient(int totalClient) {
        this.totalClient = totalClient;
    }

    public Integer getAddAt() {
        return addAt;
    }

    public void setAddAt(Integer addAt) {
        this.addAt = addAt;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("title", title)
                .add("productId", productId)
                .add("status", status)
                .add("totalClient", totalClient)
                .add("addAt", addAt)
                .add("objectId", objectId)
                .toString();
    }
}
