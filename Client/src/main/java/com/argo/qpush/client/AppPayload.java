package com.argo.qpush.client;

import com.google.common.base.Objects;
import org.msgpack.annotation.Message;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 *
 * 要和服务端的PayloadMessage保持一致
 *
 * Created by yaming_deng on 14-9-10.
 */
@Message
public class AppPayload implements Serializable {

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
     * Topic标示.
     */
    public Integer topicObjectId;
    /**
     * 是否广播
     */
    public Boolean broadcast;

    /**
     * 当Client是离线状态时. 怎么处理消息
     * @OfflineMode
     */
    public Integer offlineMode = OfflineMode.Ignore.ordinal();

    /**
     * 发送到人得状态.(0:所有,1:在线)
     */
    public Integer toMode = 0;

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("title", title)
                .add("badge", badge)
                .add("sound", sound)
                .add("clients", clients)
                .add("ext", ext)
                .add("topicObjectId", topicObjectId)
                .add("broadcast", broadcast)
                .add("offlineMode", offlineMode)
                .add("toMode", toMode)
                .toString();
    }
}
