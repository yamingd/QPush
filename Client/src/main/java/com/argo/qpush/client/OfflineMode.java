package com.argo.qpush.client;

/**
 * Created by user on 4/28/15.
 */
public enum OfflineMode {
    /**
     * 忽略消息
     */
    Ignore(0),
    /**
     * 通过 APNS 推送
     */
    APNS(1),
    /**
     * 等上线了再推送
     */
    SendAfterOnline(2);

    private int value;

    OfflineMode(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "OfflineModes{" +
                "value=" + value +
                '}';
    }
}
