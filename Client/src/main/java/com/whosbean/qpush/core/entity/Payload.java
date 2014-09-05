package com.whosbean.qpush.core.entity;


import com.whosbean.qpush.client.ChannelHolder;
import org.msgpack.annotation.Message;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * 客户端消息结构体.
 *
 * Payload payload = new Payload();
 * ...
 * payload.send()
 *
 * Created by yaming_deng on 14-8-11.
 */
@Message
public class Payload implements Serializable {

    /**
     * {
     *  "title": "abc",
     *  "badge": 10,
     *  "sound": "x.acf",
     *  "appkey": "abcdef",
     *  "broadcast": 0/1
     *  "clients" : [
     *      "ab0",
     *      "ab1"
     *  ]
     *  "ext": {
     *     "key1": "value1",
     *     "key2": "value2"
     *  }
     *  }
     */

    /**
     * 信息标题
     */
    private String title;
    /**
     * 显示数字
     */
    private Integer badge;
    /**
     * 提示音文件
     */
    private String sound;
    /**
     * 产品应用key
     */
    private String appKey;
    /**
     * 是否是广播(1/0)
     */
    private Integer broadcast;
    /**
     * 推送到的客户端用户(通常是用户id)标示（如是广播请忽略)
     */
    private List<String> clients;
    /**
     * 扩展信息.如记录id等.
     */
    private Map<String, String> ext;

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

    public String getSound() {
        return sound;
    }

    public void setSound(String sound) {
        this.sound = sound;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }

    public Integer getBroadcast() {
        return broadcast;
    }

    public void setBroadcast(Integer broadcast) {
        this.broadcast = broadcast;
    }

    public List<String> getClients() {
        return clients;
    }

    public void setClients(List<String> clients) {
        this.clients = clients;
    }

    public Map<String, String> getExt() {
        return ext;
    }

    public void setExt(Map<String, String> ext) {
        this.ext = ext;
    }


    public boolean send() throws IOException {
        return ChannelHolder.send(this);
    }
}
