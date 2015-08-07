package com.argo.qpush.client;

import org.msgpack.annotation.MessagePackMessage;

/**
 * Created by yamingd on 8/6/15.
 */
@MessagePackMessage
public class AppRequest {

    public static final int APP_REQUEST_TYPE_PAYLOAD = 0x1;
    public static final int APP_REQUEST_TYPE_NEW_TOPIC = 0x2;
    public static final int APP_REQUEST_TYPE_NEW_TOPIC_CLIENT = 0x3;
    public static final int APP_REQUEST_TYPE_REM_TOPIC = 0x4;
    public static final int APP_REQUEST_TYPE_REM_TOPIC_CLIENT = 0x5;

    private String appkey;
    private int typeId;
    private byte[] data;

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

}
