package com.argo.qpush.core.service;

import com.argo.qpush.core.MetricBuilder;
import com.argo.qpush.core.TxMain;
import com.argo.qpush.core.entity.Payload;
import com.argo.qpush.core.entity.PayloadHistory;
import com.argo.qpush.core.entity.PayloadStatus;
import com.argo.qpush.core.entity.PushStatus;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Calendar;
import java.util.List;

/**
 * Created by yaming_deng on 14-8-8.
 */
@Service
public class PayloadServiceImpl extends BaseService implements PayloadService {

    public static final String SQL_UPDATE_PAYLOAD_STATUS = "update payload set statusId=?, totalUsers = ?, sentDate=? where id = ?";
    public static final String SQL_UPDATE_PAYLOAD_CLIENT_STATUS = "update payload_client set tryLimit=tryLimit-1, statusId=?, onlineMode=?, errorId=?, errorMsg=? where payloadId = ? and userId = ?";

    public static final String SQL_PAYLOAD_INSERT = "insert into payload(id, title, badge, extras, sound, productId, totalUsers, createAt, statusId, broadcast, sentDate, offlineMode, toMode, apnsMode)values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    public static final String SQL_PAYLOAD_CLIENT_INSERT = "insert into payload_client(payloadId, userId, productId, statusId, createTime)values(?, ?, ?, ?, ?)";
    public static final String SQL_FIND_LATEST_OFFLINE_LIST = "select payloadId from payload_client where productId=? and userId = ? and onlineMode=? and createTime >= ? order by id desc limit 0, 100";

    public static PayloadService instance;

    protected static final RowMapper<Payload> Payload_ROWMAPPER = new BeanPropertyRowMapper<Payload>(
            Payload.class);

    protected static final RowMapper<PayloadHistory> PayloadHistory_ROWMAPPER = new BeanPropertyRowMapper<PayloadHistory>(
            PayloadHistory.class);

    @Override
    public Payload getSimple(long id) {
        String sql = "select * from payload where id = ?";
        List<Payload> list = mainJdbc.query(sql, Payload_ROWMAPPER, id);
        if(list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    @Override
    public List<Payload> getSimpleList(List<Long> ids) {
        StringBuilder sql = new StringBuilder("select * from payload where ");
        for (Long id : ids){
            sql.append(" id=? or ");
        }
        sql.setLength(sql.length() - 3);
        List<Payload> list = mainJdbc.query(sql.toString().intern(), ids.toArray(new Object[0]), Payload_ROWMAPPER);
        return list;
    }

    @Override
    public Payload get(long id){
        String sql = "select * from payload where id = ?";
        List<Payload> list = mainJdbc.query(sql, Payload_ROWMAPPER, id);
        if(list.size() > 0){
            Payload payload = list.get(0);
            if (payload.getTotalUsers() > 0){
                sql = "select userId from payload_client where id = ?";
                List<String> clients = mainJdbc.queryForList(sql, String.class, id);
                payload.setClients(clients);
            }
            return payload;
        }
        return null;
    }

    @Override
    public void add(final Payload payload){
        if (payload == null){
            return;
        }

        jdbcExecutor.submit(new Runnable() {
            @Override
            public void run() {

                try {
                    postAddPayload(payload);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            }
        });

    }

    @TxMain
    private void postAddPayload(final Payload payload) {
        mainJdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(
                    Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(SQL_PAYLOAD_INSERT,
                        Statement.RETURN_GENERATED_KEYS);

                ps.setObject(1, payload.getId());
                ps.setObject(2, payload.getTitle());
                ps.setObject(3, payload.getBadge());
                ps.setObject(4, payload.getExtras());
                ps.setObject(5, payload.getSound());
                ps.setObject(6, payload.getProductId());
                ps.setObject(7, payload.getTotalUsers());
                ps.setObject(8, payload.getCreateAt());
                ps.setObject(9, payload.getStatusId());
                ps.setObject(10, payload.getBroadcast());
                ps.setObject(11, payload.getSentDate());
                ps.setObject(12, payload.getOfflineMode());
                ps.setObject(13, payload.getToMode());
                ps.setObject(14, payload.getApnsMode());

                return ps;
            }
        });

        if (payload.getClients() != null){

            mainJdbc.batchUpdate(SQL_PAYLOAD_CLIENT_INSERT, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    preparedStatement.setObject(1, payload.getId());
                    preparedStatement.setObject(2, payload.getClients().get(i));
                    preparedStatement.setObject(3, payload.getProductId());
                    preparedStatement.setObject(4, 0);
                    preparedStatement.setObject(5, System.currentTimeMillis()/1000);
                }

                @Override
                public int getBatchSize() {
                    return payload.getClients().size();
                }
            });
        }
    }

    @Override
    public void saveAfterSent(final Payload payload) throws Exception {

        if (payload == null){
            return;
        }

        if (payload.getId() == null || payload.getId().intValue() == 0){
            throw new Exception("saveAfterSent needs payload to be having id value.");
        }

        jdbcExecutor.submit(new Runnable() {
            @Override
            public void run() {

                postSaveAfterSent(payload);

            }
        });

    }

    @TxMain
    private void postSaveAfterSent(final Payload payload) {
        try {
            mainJdbc.update(new PreparedStatementCreator() {
                @Override
                public PreparedStatement createPreparedStatement(
                        Connection connection) throws SQLException {

                    PreparedStatement ps = connection.prepareStatement(SQL_PAYLOAD_INSERT,
                            Statement.RETURN_GENERATED_KEYS);

                    ps.setObject(1, payload.getId());
                    ps.setObject(2, payload.getTitle());
                    ps.setObject(3, payload.getBadge());
                    ps.setObject(4, payload.getExtras());
                    ps.setObject(5, payload.getSound());
                    ps.setObject(6, payload.getProductId());
                    ps.setObject(7, payload.getTotalUsers());
                    ps.setObject(8, payload.getCreateAt());
                    ps.setObject(9, payload.getStatusId());
                    ps.setObject(10, payload.getBroadcast());
                    ps.setObject(11, payload.getSentDate());
                    ps.setObject(12, payload.getOfflineMode());
                    ps.setObject(13, payload.getToMode());
                    ps.setObject(14, payload.getApnsMode());

                    return ps;
                }
            });
        } catch (DataAccessException e) {
            logger.error("SQL_PAYLOAD_INSERT", e);
        }

        MetricBuilder.jdbcUpdateMeter.mark(1);

        if (payload.getClients() != null){

            try {
                mainJdbc.batchUpdate(SQL_PAYLOAD_CLIENT_INSERT, new BatchPreparedStatementSetter() {

                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {

                        String userId = payload.getClients().get(i);
                        PushStatus error = payload.getStatus().get(userId);
                        int statusId = error.getPayloadStatus();
                        int onlineMode = error.getOnlineMode(payload.getOfflineMode().intValue());

                        preparedStatement.setObject(1, payload.getId());
                        preparedStatement.setObject(2, userId);
                        preparedStatement.setObject(3, payload.getProductId());
                        preparedStatement.setObject(4, statusId);
                        preparedStatement.setObject(5, System.currentTimeMillis()/1000);
                        preparedStatement.setObject(6, onlineMode);
                        preparedStatement.setObject(7, error != null ? error.getCode() : null);
                        preparedStatement.setObject(8, error != null ? error.getMsg() : null);
                    }

                    @Override
                    public int getBatchSize() {
                        return payload.getClients().size();
                    }
                });

            } catch (DataAccessException e) {
                logger.error("SQL_PAYLOAD_CLIENT_INSERT", e);
            }

            MetricBuilder.jdbcUpdateMeter.mark(1);
        }
    }

    @Override
    public List<Payload> findNormalList(int productId, long start, int page, int limit){
        String sql = "select * from payload where productId = ? and broadcast=? and statusId=? and id > ? order by id limit ?, ?";
        int offset = (page - 1) * limit;
        return mainJdbc.query(sql, Payload_ROWMAPPER, productId, 0, PayloadStatus.Pending, start, offset, limit);
    }

    @Override
    public List<Payload> findBrodcastList(int productId, long start, int page, int limit){
        String sql = "select * from payload where productId = ? and broadcast=? and statusId=? and id > ? order by id limit ?, ?";
        int offset = (page - 1) * limit;
        return mainJdbc.query(sql, Payload_ROWMAPPER, productId, 1, PayloadStatus.Pending, start, offset, limit);
    }

    @Override
    public void updateSendStatus(final Payload payload) {

        jdbcExecutor.submit(new Runnable() {

            @Override
            public void run() {

                postUpdateSendStatus(payload);

            }

        });

    }

    @TxMain
    private void postUpdateSendStatus(final Payload payload) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateSendStatus, payloadId={}", payload.getId());
        }

        int total = payload.getStatus().size();

        mainJdbc.update(SQL_UPDATE_PAYLOAD_STATUS, PayloadStatus.Sent, total, System.currentTimeMillis() / 1000, payload.getId());

        try {

            mainJdbc.batchUpdate(SQL_UPDATE_PAYLOAD_CLIENT_STATUS, new BatchPreparedStatementSetter() {

                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {

                    String userId = payload.getClients().get(i);
                    PushStatus error = payload.getStatus().get(userId);
                    int statusId = error.getPayloadStatus();
                    int onlineMode = error.getOnlineMode(payload.getOfflineMode().intValue());

                    preparedStatement.setObject(1, statusId);
                    preparedStatement.setObject(2, onlineMode);
                    preparedStatement.setObject(3, error != null ? error.getCode() : null);
                    preparedStatement.setObject(4, error != null ? error.getMsg() : null);

                    preparedStatement.setObject(5, payload.getId());
                    preparedStatement.setObject(6, userId);

                }

                @Override
                public int getBatchSize() {
                    return payload.getClients().size();
                }
            });

        } catch (DataAccessException e) {
            logger.error("SQL_PAYLOAD_CLIENT_INSERT", e);
        }


        MetricBuilder.jdbcUpdateMeter.mark(2);

        if (logger.isDebugEnabled()) {
            logger.debug("updateSendStatus OK, payloadId={}", payload.getId());
        }
    }

    @Override
    public void updateSendStatus(final Payload message, final String userId, final PushStatus error) {

        jdbcExecutor.submit(new Runnable() {

            @Override
            public void run() {

                postUpdateSendStatus(message, userId, error);

            }
        });

    }

    @TxMain
    private void postUpdateSendStatus(Payload message, String userId, PushStatus error) {
        if (logger.isDebugEnabled()) {
            logger.debug("updateSendStatus, payloadId={}, userId={}", message.getId(), userId);
        }

        int statusId = error.getPayloadStatus();
        int onlineMode = error.getOnlineMode(message.getOfflineMode().intValue());

        try {

            mainJdbc.update(SQL_UPDATE_PAYLOAD_CLIENT_STATUS,
                            statusId, onlineMode,
                            error != null ? error.getCode() : null,
                            error != null ? error.getMsg() : null,
                            message.getId(),
                            userId);

        } catch (DataAccessException e) {
            logger.error("UpdateSendStatus Error.", e);
        }

        MetricBuilder.jdbcUpdateMeter.mark(1);

        if (logger.isDebugEnabled()) {
            logger.debug("updateSendStatus OK, payloadId={}, userId={}", message.getId(), userId);
        }
    }

    @Override
    public List<Long> findLatestToOfflineClients(int productId, String userId, long start){
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_MONTH, -3);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);

        long ts = calendar.getTime().getTime() / 1000;

        List<Long> list = this.mainJdbc.queryForList(SQL_FIND_LATEST_OFFLINE_LIST, Long.class, productId, userId, 1, ts);
        return list;
    }

    private volatile boolean stopping = false;

    @Override
    public void destroy() throws Exception {
        super.destroy();
        stopping = true;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }
}
