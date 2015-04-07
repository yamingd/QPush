package com.argo.qpush.core.service;

import com.argo.qpush.core.MetricBuilder;
import com.argo.qpush.core.TxMain;
import com.argo.qpush.core.entity.Payload;
import com.argo.qpush.core.entity.PayloadHistory;
import com.argo.qpush.core.entity.PayloadStatus;
import com.argo.qpush.core.entity.PushError;
import com.google.common.collect.Lists;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yaming_deng on 14-8-8.
 */
@Service
public class PayloadServiceImpl extends BaseService implements PayloadService {

    public static PayloadService instance;

    protected static final RowMapper<Payload> Payload_ROWMAPPER = new BeanPropertyRowMapper<Payload>(
            Payload.class);

    protected static final RowMapper<PayloadHistory> PayloadHistory_ROWMAPPER = new BeanPropertyRowMapper<PayloadHistory>(
            PayloadHistory.class);

    private ThreadPoolTaskExecutor jdbcExecutor = null;
    private AtomicLong jdbcPending = new AtomicLong();

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
    @TxMain
    public void add(final Payload payload){
        if (payload == null){
            return;
        }

        final String sql = "insert into payload(title, badge, extras, sound, productId, totalUsers, createAt, statusId, broadcast)values(?, ?, ?, ?, ?, ?, ?, ?, ?)";

        KeyHolder holder = new GeneratedKeyHolder();
        this.mainJdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(
                    Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setObject(1, payload.getTitle());
                ps.setObject(2, payload.getBadge());
                ps.setObject(3, payload.getExtras());
                ps.setObject(4, payload.getSound());
                ps.setObject(5, payload.getProductId());
                ps.setObject(6, 0);
                ps.setObject(7, new Date().getTime()/1000);
                ps.setObject(8, PayloadStatus.Pending);
                ps.setObject(9, payload.getBroadcast());
                return ps;
            }
        }, holder);

        payload.setId(holder.getKey().longValue());

        if (payload.getClients() != null){
            List<Object[]> args = Lists.newArrayList();
            final String sql0 = "insert into payload_client(payloadId, userId, productId)values(?, ?, ?)";
            for(String userId : payload.getClients()){
                args.add(new Object[]{payload.getId(), userId, payload.getProductId()});
            }
            this.mainJdbc.batchUpdate(sql0, args);
        }
    }

    @TxMain
    private void updatePendingCount(boolean incr){
        long count = incr ? jdbcPending.incrementAndGet() : jdbcPending.decrementAndGet();
        logger.info("JdbcExecutor Pending. total=" + count);
    }

    @Override
    @TxMain
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

                final String sql = "insert into payload(id, title, badge, extras, sound, productId, totalUsers, createAt, statusId, broadcast, sentDate)values(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

                mainJdbc.update(new PreparedStatementCreator() {
                    @Override
                    public PreparedStatement createPreparedStatement(
                            Connection connection) throws SQLException {
                        PreparedStatement ps = connection.prepareStatement(sql,
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
                        return ps;
                    }
                });

                MetricBuilder.jdbcUpdateMeter.mark(1);

                if (payload.getClients() != null){
                    List<Object[]> args = Lists.newArrayList();
                    final String sql0 = "insert into payload_client(payloadId, userId, productId, statusId, createTime, errorId, errorMsg)values(?, ?, ?, ?, ?, ?, ?)";
                    for(String userId : payload.getClients()){
                        PushError error = payload.getFailedClients().get(userId);
                        int statusId = error != null ? 0 : 1;
                        args.add(new Object[]{payload.getId(), userId, payload.getProductId(), statusId, new Date().getTime()/1000,
                                error != null ? error.getCode() : null,
                                error != null ? error.getMsg() : null});
                    }
                    mainJdbc.batchUpdate(sql0, args);
                    MetricBuilder.jdbcUpdateMeter.mark(1);
                }

                updatePendingCount(false);
            }
        });

        updatePendingCount(true);

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
    @TxMain
    public void updateSendStatus(final Payload message, final int counting) {

        jdbcExecutor.submit(new Runnable() {
            @Override
            public void run() {

                String sql = "update payload set statusId=?, totalUsers=totalUsers+?, sentDate=? where id = ?";
                mainJdbc.update(sql, counting > 0 ? PayloadStatus.Sent : PayloadStatus.Pending, counting, new Date().getTime()/1000, message.getId());

                sql = "update payload_client set statusId=?, createTime=?, errorId=?, errorMsg=? where payloadId = ? and userId = ?";

                List<Object[]> args = Lists.newArrayList();
                for(String userId : message.getClients()){
                    PushError error = message.getFailedClients().get(userId);
                    int statusId = error != null ? 0 : 1;
                    args.add(new Object[]{statusId, new Date().getTime()/1000,
                            error != null ? error.getCode() : null,
                            error != null ? error.getMsg() : null,
                            message.getId(), userId});
                }
                mainJdbc.batchUpdate(sql, args);

                MetricBuilder.jdbcUpdateMeter.mark(2);

                updatePendingCount(false);

                if (logger.isDebugEnabled()){
                    logger.debug("updateSendStatus OK!");
                }
            }
        });

        updatePendingCount(true);

    }

    @Override
    public List<Long> findLatest(int productId, String userId, long start){
        String sql = "select id from payload_client where productId=? and userId = ? and statusId=? and id > ? order by id desc limit 0, 10";
        List<Long> list = this.mainJdbc.queryForList(sql, Long.class, productId, userId, PayloadStatus.Pending, start);
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
        int limit = Integer.parseInt(appConfigs.getProperty("jdbc.executors", "100"));

        //实际扫描线程池
        jdbcExecutor = new ThreadPoolTaskExecutor();
        jdbcExecutor.setCorePoolSize(limit/5);
        jdbcExecutor.setMaxPoolSize(limit);
        jdbcExecutor.setWaitForTasksToCompleteOnShutdown(true);
        jdbcExecutor.afterPropertiesSet();

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (!stopping) {
                    long total = jdbcPending.get();
                    logger.info("JdbcExecutor Pending. total=" + total);
                    try {
                        Thread.sleep(10 * 1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        thread.start();
    }
}
