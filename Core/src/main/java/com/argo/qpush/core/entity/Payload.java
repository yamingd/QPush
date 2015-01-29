package com.argo.qpush.core.entity;

import com.argo.qpush.client.PayloadMessage;
import com.argo.qpush.core.MessageUtils;
import com.argo.qpush.protobuf.PBAPNSBody;
import com.argo.qpush.protobuf.PBAPNSMessage;
import com.argo.qpush.protobuf.PBAPNSUserInfo;
import com.google.common.collect.Maps;
import org.apache.commons.lang3.StringUtils;
import org.msgpack.annotation.MessagePackMessage;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 消息结构体. 参考苹果消息规范
 * Created by yaming_deng on 14-8-6.
 */
@MessagePackMessage
public class Payload implements Serializable {
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
    /**
     * 是否是广播(1/0)
     */
    private Integer broadcast;
    private Long sentDate;

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
     * @see com.argo.qpush.core.entity.PayloadStatus
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

    public Long getSentDate() {
        return sentDate;
    }

    public void setSentDate(Long sentDate) {
        this.sentDate = sentDate;
    }


    public PBAPNSMessage asAPNSMessage(){
        PBAPNSMessage.Builder builder = PBAPNSMessage.newBuilder();
        if (this.badge == null){
            this.badge = 0;
        }
        builder.setAps(PBAPNSBody.newBuilder().setBadge(this.badge).setAlert(this.title).setSound(this.sound));
        if (StringUtils.isNotBlank(this.extras)){
            Map<String, String> tmp = MessageUtils.asT(Map.class, this.extras);
            for(String key : tmp.keySet()){
                builder.addUserInfo(PBAPNSUserInfo.newBuilder().setKey(key).setValue(tmp.get(key)));
            }
        }
        return builder.build();
    }

    public String asJson(){
        Map<String, Object> builder = new HashMap<String, Object>();
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("badge", this.badge);
        map.put("alert", this.title);
        map.put("sound", this.sound);
        builder.put("aps", map);

        if (StringUtils.isNotBlank(this.extras)){
            Map<String, String> tmp = MessageUtils.asT(Map.class, this.extras);
            builder.put("userInfo", tmp);
        }

        return MessageUtils.toJson(builder);
    }

    public Payload() {
    }

    public Payload(PayloadMessage message){
        this.title = message.title;
        this.badge = message.badge;
        this.sound = message.sound;
        this.clients = message.clients;
        this.extras = MessageUtils.toJson(message.ext);
        this.broadcast = message.broadcast == null || !message.broadcast ? 0 : 1;
    }

    private Map<String, PushError> failedClients = Maps.newConcurrentMap();

    public Map<String, PushError> getFailedClients() {
        return failedClients;
    }

    public void addFailedClient(String userId, PushError error){
        failedClients.put(userId, error);
    }

    @Override
    public String toString() {
        return "Payload{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", badge=" + badge +
                ", extras='" + extras + '\'' +
                ", sound='" + sound + '\'' +
                ", productId=" + productId +
                ", totalUsers=" + totalUsers +
                ", createAt=" + createAt +
                ", statusId=" + statusId +
                ", clients=" + clients +
                ", broadcast=" + broadcast +
                ", sentDate=" + sentDate +
                '}';
    }
}
