package com.argo.qpush.core.entity;

import com.google.common.base.Objects;
import org.msgpack.annotation.MessagePackMessage;

import java.io.Serializable;

/**
 * Created by yamingd on 8/6/15.
 */
@MessagePackMessage
public class TopicClient implements Serializable {

    private Integer id;
    private Integer topicId;
    private Integer clientId;
    private String userId;
    private Integer addAt;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getTopicId() {
        return topicId;
    }

    public void setTopicId(Integer topicId) {
        this.topicId = topicId;
    }

    public Integer getClientId() {
        return clientId;
    }

    public void setClientId(Integer clientId) {
        this.clientId = clientId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getAddAt() {
        return addAt;
    }

    public void setAddAt(Integer addAt) {
        this.addAt = addAt;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("id", id)
                .add("topicId", topicId)
                .add("clientId", clientId)
                .add("userId", userId)
                .add("addAt", addAt)
                .toString();
    }
}
