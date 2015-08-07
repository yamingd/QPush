package com.argo.qpush.core.service;

import com.argo.qpush.core.TxMain;
import com.argo.qpush.core.entity.Product;
import com.google.common.collect.Maps;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by yaming_deng on 14-8-8.
 */
@Service
public class ProductServiceImpl extends BaseService implements ProductService {

    public static ProductService instance;

    private ConcurrentMap<String, Product> productIdMaps = Maps.newConcurrentMap();

    protected static final RowMapper<Product> Product_ROWMAPPER = new BeanPropertyRowMapper<Product>(
            Product.class);

    @Override
    public Integer getProductId(String key) {
        Product product = productIdMaps.get(key);
        if (null == product){
            product = findByKey(key);
            if (null != product) {
                productIdMaps.put(key, product);
                return product.getId();
            }
            logger.error("Product not found. appkey=" + key);
            return null;
        }
        return product.getId();
    }

    @Override
    public Product findByKey(String key){
        String cachekey = String.format("prod:%s", key);
        Product product = this.getItemFromRedis(cachekey, Product.class);
        String sql = "select * from product where appKey = ?";
        List<Product> list = this.mainJdbc.query(sql, Product_ROWMAPPER, key);
        if (list.size() > 0){
            product = list.get(0);
            this.putItemToRedis(cachekey, product);
        }

        return product;
    }

    @Override
    @TxMain
    public void add(final Product product){
        if (product == null){
            return;
        }

        product.setAppKey(UUID.randomUUID().toString().replace("-", ""));
        product.setSecret(UUID.randomUUID().toString().replace("-", ""));

        final String sql = "insert into product(title, appKey, secret, clientTypeid, certPath, devCertPath)values(?, ?, ?, ?, ?, ?)";
        KeyHolder holder = new GeneratedKeyHolder();
        this.mainJdbc.update(new PreparedStatementCreator() {
            @Override
            public PreparedStatement createPreparedStatement(
                    Connection connection) throws SQLException {
                PreparedStatement ps = connection.prepareStatement(sql,
                        Statement.RETURN_GENERATED_KEYS);
                ps.setObject(1, product.getTitle());
                ps.setObject(2, product.getAppKey());
                ps.setObject(3, product.getSecret());
                ps.setObject(4, product.getClientTypeid());
                ps.setObject(5, product.getCertPath());
                ps.setObject(6, product.getDevCertPath());
                return ps;
            }
        }, holder);

        product.setId(holder.getKey().intValue());
    }

    @Override
    public List<Product> findAll(){
        String sql = "select * from product order by id";
        List<Product> list = this.mainJdbc.query(sql, Product_ROWMAPPER);
        return list;
    }

    @Override
    public List<Product> findNewest(int startId){
        String sql = "select * from product where id > ? order by id";
        List<Product> list = this.mainJdbc.query(sql, Product_ROWMAPPER, startId);
        return list;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<Product> list = this.findAll();
        for (Product item : list){
            productIdMaps.put(item.getAppKey(), item);
        }
        instance = this;
    }
}
