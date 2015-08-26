package com.argo.qpush.client;

import com.google.common.base.Objects;
import org.msgpack.annotation.MessagePackMessage;

/**
 * Created by yamingd on 8/6/15.
 */
@MessagePackMessage
public class RequestMessage {

    public static final int REQUEST_TYPE_PAYLOAD = 0x1;
    public static final int REQUEST_TYPE_NEW_TOPIC = 0x2;
    public static final int REQUEST_TYPE_NEW_TOPIC_CLIENT = 0x3;
    public static final int REQUEST_TYPE_REM_TOPIC = 0x4;
    public static final int REQUEST_TYPE_REM_TOPIC_CLIENT = 0x5;

    private String appkey;
    private int typeId;
    private byte[] data;

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

    public String getAppkey() {
        return appkey;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this)
                .add("appkey", appkey)
                .add("typeId", typeId)
                .toString();
    }
}
