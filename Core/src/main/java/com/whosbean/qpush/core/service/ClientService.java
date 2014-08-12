package com.whosbean.qpush.core.service;

import com.whosbean.qpush.core.entity.Client;
import com.whosbean.qpush.core.entity.ClientStatus;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

/**
 * 客户端管理类.
 * Created by yaming_deng on 14-8-11.
 */
public class ClientService extends BaseService {

    public static ClientService instance;

    protected static final RowMapper<Client> Client_ROWMAPPER = new BeanPropertyRowMapper<Client>(
            Client.class);

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    public void add(final Client client){
        if (client == null){
            return;
        }

        final String sql = "insert into client(productId, userId, deviceToken, createAt, statusId, typeId)values(?, ?, ?, ?, ?, ?)";
        KeyHolder holder = new GeneratedKeyHolder();
        this.mainJdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(
                    Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setObject(0, client.getProductId());
                ps.setObject(1, client.getUserId());
                ps.setObject(2, client.getDeviceToken());
                ps.setObject(3, new Date());
                ps.setObject(4, ClientStatus.NewlyAdd);
                ps.setObject(5, client.getTypeId());
                return ps;
            }
        }, holder);

        client.setId(holder.getKey().longValue());
    }

    public Client findByUserId(String userId){
        String sql = "select * from client where userId = ?";
        List<Client> list = this.mainJdbc.query(sql, Client_ROWMAPPER, userId);
        if (list.size() > 0){
            return list.get(0);
        }
        return null;
    }

    public void updateOnlineTs(long id){
        String sql = "update client set lastOnline=? where id = ?";
        this.mainJdbc.update(sql, new Date(), id);
    }
}
