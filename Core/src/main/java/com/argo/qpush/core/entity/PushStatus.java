package com.argo.qpush.core.entity;

import com.argo.qpush.protobuf.PBAPNSMessage;
import org.msgpack.annotation.MessagePackMessage;

/**
 * Created by user on 1/29/15.
 */
@MessagePackMessage
public class PushStatus {

    public static final int TcpSent = 0;

    public static final int Success = 1;

    public static final int Ignore = 2;

    public static final int Offline  = 11;

    public static final int WriterError = 12;

    public static final int NoConnections = 13;

    public static final int ChannelClosed = 14;

    public static final int UnKnown = 15;

    public static final int NO_DEVICE_TOKEN = 16;

    public static final int iOSPushError = 17;

    public static final int NoClient = 18;

    public static final int iOSPushConfigError = 19;

    public static final int WaitOnline = 10;

    public static final int APNSSent = 20;

    public static final int APNSTokenInvalid = 21;

    private int code;
    private String msg;

    public PushStatus() {

    }

    public PushStatus(int code) {
        this.code = code;
    }

    public PushStatus(int code, String msg){
        this.code = code;
        this.msg = msg;
    }

    public int getPayloadStatus(){

        if (this.code == APNSSent || this.code == TcpSent || this.code == Success){
            return PayloadStatus.Sent;
        }

        return PayloadStatus.Failed;
    }

    public int getOnlineMode(int payloadSetting){

        if (getCode() == NoClient
                || getCode() == NO_DEVICE_TOKEN
                || getCode() == WaitOnline){

            //离线消息在用户上线时的处理方式
            if (payloadSetting == PBAPNSMessage.OfflineModes.Ignore_VALUE){
                return  0; //忽略
            }else{
                return  1; //发送
            }
        }else {

            return 0;

        }
    }

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("PushStatus{");
        sb.append("code=").append(code);
        sb.append(", msg='").append(msg).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
