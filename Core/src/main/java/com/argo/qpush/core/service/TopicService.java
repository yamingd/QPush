package com.argo.qpush.core.service;

import com.argo.qpush.core.entity.Topic;
import org.springframework.dao.DataAccessException;

import java.util.List;

/**
 * Created by yamingd on 8/6/15.
 */
public interface TopicService {
    /**
     *
     * @param productId
     * @param objectId
     * @return
     */
    Topic findByObjectId(Integer productId, Long objectId);

    /**
     * 新建Topic
     * @param topic
     * @param clientIds
     */
    void newTopic(Topic topic, List<String> clientIds) throws DataAccessException;

    /**
     * 新加入Topic Client
     * @param topic
     * @param clientIds
     */
    void newTopicClients(Topic topic, List<String> clientIds) throws DataAccessException;

    /**
     * 删除Topic
     * @param topic
     */
    void removeTopic(Topic topic) throws DataAccessException;

    /**
     * 删除Topic Client
     * @param topic
     * @param clientIds
     */
    void removeTopicClients(Topic topic, List<String> clientIds) throws DataAccessException;
}
