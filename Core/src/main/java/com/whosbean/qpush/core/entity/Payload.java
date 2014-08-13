package com.whosbean.qpush.core.entity;

import com.whosbean.qpush.core.GsonUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息结构体. 参考苹果消息规范
 * Created by yaming_deng on 14-8-6.
 */
public class Payload {

    /**
     * 消息id
     */
    private Long id;
    /**
     * 在客户端信息中心显示的标题
     */
    private String title;
    /**
     * 消息提示数. 显示在应用图标上.
     */
    private Integer badge;
    /**
     * 扩展信息.
     */
    private String extras;
    /**
     * 提示音
     */
    private String sound;
    /**
     * 产品id
     */
    private Integer productId;
    /**
     * 要推送到的用户数.
     */
    private Integer totalUsers;
    /**
     * 创建时间.
     */
    private Long createAt;
    /**
     * 发送状态.
     */
    private Integer statusId;
    /**
     * 要接收的客户端
     */
    private List<String> clients;
    private Map<String, Object> ext;
    private String appkey;
    /**
     * 是否是广播(1/0)
     */
    private Integer broadcast;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getStatusId() {
        return statusId;
    }

    /**
     * @see com.whosbean.qpush.core.entity.PayloadStatus
     * @param statusId
     */
    public void setStatusId(Integer statusId) {
        this.statusId = statusId;
    }

    public Long getCreateAt() {
        return createAt;
    }

    public void setCreateAt(Long createAt) {
        this.createAt = createAt;
    }

    public Integer getProductId() {
        return productId;
    }

    public void setProductId(Integer productId) {
        this.productId = productId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getBadge() {
        return badge;
    }

    public void setBadge(Integer badge) {
        this.badge = badge;
    }

    public String getExtras() {
        return extras;
    }

    public void setExtras(String extras) {
        this.extras = extras;
    }

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public Integer getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Integer totalUsers) {
        this.totalUsers = totalUsers;
    }

    public List<String> getClients() {
        return clients;
    }

    public void setClients(List<String> clients) {
        this.clients = clients;
    }

    public Integer getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(Integer broadcast) {
        this.broadcast = broadcast;
    }

    public Map<String, Object> getExt() {
        return ext;
    }

    public void setExt(Map<String, Object> ext) {
        this.ext = ext;
    }

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public Map asStdMap(){
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> aps = new HashMap<String, Object>();
        aps.put("alert", this.getTitle());
        aps.put("badge", this.getBadge());
        aps.put("sound", this.getSound());
        map.put("aps", aps);
        map.put("userInfo", GsonUtils.asT(Map.class, this.getExtras()));
        return map;
    }
}
