package com.whosbean.qpush.core.entity;

import com.whosbean.qpush.apns.APNSMessage;
import com.whosbean.qpush.client.PayloadMessage;
import com.whosbean.qpush.core.MessageUtils;
import org.msgpack.annotation.MessagePackMessage;

import java.io.Serializable;
import java.util.List;

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

    public Long getSentDate() {
        return sentDate;
    }

    public void setSentDate(Long sentDate) {
        this.sentDate = sentDate;
    }

    public APNSMessage asAPNSMessage(){
        return new APNSMessage(this);
    }

    public Payload() {
    }

    public Payload(PayloadMessage message){
        this.title = message.title;
        this.badge = message.badge;
        this.sound = message.sound;
        this.clients = message.clients;
        this.extras = MessageUtils.toJson(message.ext);
        this.broadcast = message.broadcast ? 1 : 0;
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
