package com.argo.qpush.client;

/**
 * Created by user on 4/28/15.
 */
public enum APNSMode {
    /**
     * 所有
     */
    All(0),
    /**
     * 仅是登录的人才推送
     */
    Signined(1);

    private int value;

    APNSMode(int value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "APNSMode{" +
                "value=" + value +
                '}';
    }
}
