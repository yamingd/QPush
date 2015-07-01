package com.argo.qpush.core.entity;

import org.msgpack.annotation.MessagePackMessage;

/**
 * Created by user on 1/29/15.
 */
@MessagePackMessage
public class PushStatus {

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

    public int getCode() {
        return code;
    }

    public String getMsg() {
        return msg;
    }

}
