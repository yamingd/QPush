package com.argo.qpush.pipe;

import com.argo.qpush.core.entity.Product;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by yaming_deng on 14-8-8.
 */
public class PayloadCursor implements Serializable {

    private Product product;
    private long startId;
    private int page;
    private int limit;
    private Date ts;

    public PayloadCursor(Product product) {
        this.product = product;
        this.startId = 0;
        this.page = 1;
        this.limit = 100;
    }

    public PayloadCursor(Product productKey, long startId, int page, int limit) {
        this.product = product;
        this.startId = startId;
        this.page = page;
        this.limit = limit;
        this.ts = new Date();
    }

    public Product getProduct() {
        return product;
    }

    public long getStartId() {
        return startId;
    }

    public void setStartId(long startId) {
        this.startId = startId;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    public Date getTs() {
        return ts;
    }

    public void setTs(Date ts) {
        this.ts = ts;
    }

    @Override
    public String toString() {
        return "PayloadCursor{" +
                "productKey='" + product.getAppKey() + '\'' +
                ", startId=" + startId +
                ", page=" + page +
                ", limit=" + limit +
                ", ts=" + ts +
                '}';
    }
}
