package com.argo.qpush.client;

import org.msgpack.annotation.Message;

import java.util.List;
import java.util.Map;

/**
 * From Java App Server
 * Created by yaming_deng on 14-9-10.
 */
@Message
public class PayloadMessage {

    /**
     * 在客户端信息中心显示的标题
     */
    public String title;
    /**
     * 消息提示数. 显示在应用图标上.
     */
    public Integer badge;
    /**
     * 提示音
     */
    public String sound;
    /**
     * 要接收的客户端
     */
    public List<String> clients;
    /**
     * 扩展信息
     */
    public Map<String, String> ext;
    /**
     * 应用标示.
     */
    public String appkey;
    /**
     * 是否广播
     */
    public Boolean broadcast;
    /**
     * 当Client是离线状态时. 怎么处理消息
     * @OfflineMode
     */
    public Integer offlineMode;

    /**
     * 发送到人得状态.(0:所有,1:在线)
     */
    public Integer toMode;

    @Override
    public String toString() {
        return "PayloadMessage{" +
                "title='" + title + '\'' +
                ", badge=" + badge +
                ", sound='" + sound + '\'' +
                ", clients=" + clients +
                ", ext=" + ext +
                ", appkey='" + appkey + '\'' +
                ", broadcast=" + broadcast +
                '}';
    }
}
