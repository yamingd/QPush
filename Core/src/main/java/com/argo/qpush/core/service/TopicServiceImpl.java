package com.argo.qpush.core.service;

import com.argo.qpush.core.EpochTime;
import com.argo.qpush.core.TxMain;
import com.argo.qpush.core.entity.Client;
import com.argo.qpush.core.entity.Topic;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yamingd on 8/7/15.
 */
@Service
public class TopicServiceImpl extends BaseService implements TopicService {

    public static final String SQL_findByObjectId = "select * from topic where productId = ? and objectId = ?";
    public static final String SQL_newTopic = "insert into topic(title, productId, addAt, objectId)values(?, ?, ?, ?)";
    public static final String SQL_newTopicClient = "insert into topic_client(topicId, clientId, userId, addAt)values(?, ?, ?, ?)";

    public static TopicService instance;

    protected static final RowMapper<Topic> Topic_ROWMAPPER = new BeanPropertyRowMapper<Topic>(
            Topic.class);


    @Autowired
    private ClientService clientService;


    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    @Override
    public Topic findByObjectId(Integer productId, Long objectId) {
        String cacheKey = formatCacheKey(productId, objectId);
        Topic topic = getItemFromRedis(cacheKey, Topic.class);
        if (null != topic){
            return topic;
        }
        List<Topic> list = mainJdbc.query(SQL_findByObjectId, Topic_ROWMAPPER, productId, objectId);
        if (list.size() > 0){
            topic = list.get(0);
            putItemToRedis(cacheKey, topic);
            return topic;
        }
        logger.error("can't find Topic. productId=%s, objectId=%s", productId, objectId);
        return null;
    }

    private String formatCacheKey(Integer productId, Long objectId) {
        return String.format("topic:%s:%s", productId, objectId);
    }

    @Override
    @TxMain
    public void newTopic(final Topic topic, List<String> clientIds) throws DataAccessException {

        if (topic.getId() == null){
            prepareTopic(topic);
        }

        if (clientIds != null && clientIds.size() > 0){
            newTopicClients(topic, clientIds);
        }

    }

    private void prepareTopic(final Topic topic) throws DataAccessException {
        Topic found = findByObjectId(topic.getProductId(), topic.getObjectId());
        if (found == null){
            GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();

            mainJdbc.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(Connection con) throws SQLException {

                    PreparedStatement ps = con.prepareStatement(SQL_newTopic,
                            Statement.RETURN_GENERATED_KEYS);

                    ps.setObject(1, topic.getTitle());
                    ps.setObject(2, topic.getProductId());
                    ps.setObject(3, EpochTime.now());
                    ps.setObject(4, topic.getObjectId());

                    return ps;

                }
            }, keyHolder);

            topic.setId(keyHolder.getKey().intValue());

        }else{
            topic.setId(found.getId());
            topic.setAddAt(found.getAddAt());
            topic.setTotalClient(found.getTotalClient());
            topic.setStatus(found.getStatus());
        }
    }

    @Override
    @TxMain
    public void newTopicClients(final Topic topic, final List<String> clientIds) throws DataAccessException{

        if (topic.getId() == null){
            prepareTopic(topic);
        }

        if (null != topic.getId() && clientIds != null && clientIds.size() > 0){

            final List<Client> clients = new ArrayList<Client>();
            for (int i = 0; i < clientIds.size(); i++) {
                Client client = clientService.findByUserId(clientIds.get(i));
                clients.add(client);
            }

            int[] ret = mainJdbc.batchUpdate(SQL_newTopicClient, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {

                    String userId = clientIds.get(i);
                    Client client = clients.get(i);
                    ps.setObject(1, topic.getId());
                    ps.setObject(2, client != null ? client.getId() : null);
                    ps.setObject(3, userId);
                    ps.setObject(4, EpochTime.now());

                }

                @Override
                public int getBatchSize() {
                    return clientIds.size();
                }
            });

            logger.info("newTopicClients, result=%s, request=%s", ret.length, clientIds.size());

        }

    }

    @Override
    public void removeTopic(final Topic topic) {

        jdbcExecutor.submit(new Runnable() {
            @Override
            public void run() {

                postRemoveTopic(topic);

            }
        });

    }

    @TxMain
    private void postRemoveTopic(Topic topic) {
        try {
            if (topic.getId() == null){
                prepareTopic(topic);
            }
            if (topic.getId() == null){
                return;
            }

            String sql = "update topic set status = ? where id = ?";
            mainJdbc.update(sql, 0, topic.getId());

            sql = "delete from topic_client where topicId=?";
            mainJdbc.update(sql, topic.getId());

            String cacheKey = formatCacheKey(topic.getProductId(), topic.getObjectId());
            delCache(cacheKey);

        } catch (DataAccessException e) {

            logger.error(e.getMessage(), e);

        }
    }

    @Override
    public void removeTopicClients(final Topic topic, final List<String> clientIds) {

        jdbcExecutor.submit(new Runnable() {
            @Override
            public void run() {

                postRemoveTopicClients(topic, clientIds);

            }
        });
    }

    @TxMain
    private void postRemoveTopicClients(Topic topic, List<String> clientIds) {
        try {
            if (topic.getId() == null){
                prepareTopic(topic);
            }
            if (topic.getId() == null){
                return;
            }
            String sql = "delete from topic_client where topicId=? and userId=?";
            for (int i = 0; i < clientIds.size(); i++) {
                String userId = clientIds.get(i);
                if (StringUtils.isEmpty(userId)){
                    continue;
                }
                mainJdbc.update(sql, topic.getId(), userId);
            }
        } catch (DataAccessException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
