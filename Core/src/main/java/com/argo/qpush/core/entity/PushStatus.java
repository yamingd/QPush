package com.argo.qpush.core.entity;

import com.argo.qpush.protobuf.PBAPNSMessage;
import org.msgpack.annotation.MessagePackMessage;

/**
 * Created by user on 1/29/15.
 */
@MessagePackMessage
public class PushStatus {

    /**
     * 通过TCP渠道发送出去
     */
    public static final int TcpSent = 0;
    /**
     * 发送成功(这个比较模糊)
     */
    public static final int Success = 1;
    /**
     * 忽略这个端, 和ErrorMsg一起使用
     */
    public static final int Ignore = 2;
    /**
     * 未使用
     */
    public static final int Offline  = 11;
    /**
     * TCP渠道写出数据错误, 有可能是Connection reset
     */
    public static final int WriterError = 12;
    /**
     * 端没有链接到服务
     */
    public static final int NoConnections = 13;
    /**
     * 链接的Channel已经关闭
     */
    public static final int ChannelClosed = 14;
    /**
     * 未确定的错误
     */
    public static final int UnKnown = 15;
    /**
     * 没有DeviceToken
     */
    public static final int NO_DEVICE_TOKEN = 16;
    /**
     * iOS APNS 推送错误
     */
    public static final int iOSPushError = 17;
    /**
     * 用户从没连上过服务
     */
    public static final int NoClient = 18;
    /**
     * iOS APNS 配置错误
     */
    public static final int iOSPushConfigError = 19;
    /**
     * 等待用户端上线再推送
     */
    public static final int WaitOnline = 10;
    /**
     * 通过APNS发送成功
     */
    public static final int APNSSent = 20;
    /**
     * APNS的deviceToken非法
     */
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
