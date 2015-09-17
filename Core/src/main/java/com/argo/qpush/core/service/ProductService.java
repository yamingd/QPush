package com.argo.qpush.core.service;

import com.argo.qpush.core.TxMain;
import com.argo.qpush.core.entity.Product;

import java.util.List;

/**
 * Created by user on 1/28/15.
 */
public interface ProductService {

    /**
     *
     * @param key
     * @return Integer
     */
    Integer getProductId(String key);

    /**
     *
     * @param key
     * @return Product
     */
    Product findByKey(String key);

    @TxMain
    void add(Product product);

    List<Product> findAll();

    List<Product> findNewest(int startId);
}
