package com.whosbean.qpush.core.entity;

/**
 * 定义应用产品.
 * Created by yaming_deng on 14-8-6.
 */
public class Product {

    /**
     * 产品应用唯一标示
     */
    private Integer id;
    /**
     * 产品显示名称
     */
    private String title;
    /**
     * 产品客户端唯一码
     */
    private String key;
    /**
     * 产品客户端信息加密串
     */
    private String secret;
    /**
     * 客户端类型
     */
    private Integer clientTypeid;
    /**
     * iOS APNS生产证书路径
     */
    private String certPath;
    /**
     * iOS APNS开发证书路径
     */
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

    /**
     * @see com.whosbean.qpush.core.entity.ClientType
     * @param clientTypeid
     */
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
