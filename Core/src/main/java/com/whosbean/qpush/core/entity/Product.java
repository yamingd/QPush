package com.whosbean.qpush.core.entity;

/**
 * 定义应用产品.
 * Created by yaming_deng on 14-8-6.
 */
public class Product {

    private Integer id;
    private String title;
    private String key;
    private String secret;
    private Integer clientTypeid;
    private String certPath;
    private String devCertPath;

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

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getCertPath() {
        return certPath;
    }

    public void setCertPath(String certPath) {
        this.certPath = certPath;
    }

    public String getDevCertPath() {
        return devCertPath;
    }

    public void setDevCertPath(String devCertPath) {
        this.devCertPath = devCertPath;
    }

    public Integer getClientTypeid() {
        return clientTypeid;
    }

    public void setClientTypeid(Integer clientTypeid) {
        this.clientTypeid = clientTypeid;
    }

    @Override
    public int hashCode() {
        return this.id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (obj instanceof Product){
            Product o = (Product)obj;
            return o.getId().equals(this.getId());
        }
        return false;
    }
}
