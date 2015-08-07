package com.argo.qpush.core;

/**
 * Created by yamingd on 8/7/15.
 */
public final class EpochTime {

    public static long epoch = 1420041600L;

    public static int now(){
        long ts = System.currentTimeMillis() / 1000 - epoch;
        return (int)ts;
    }
}
