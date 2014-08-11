package com.whosbean.qpush.gateway;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by yaming_deng on 14-8-8.
 */
public class ServerMetrics {

    protected static Logger logger = LoggerFactory.getLogger(ServerMetrics.class);

    private static AtomicLong totalConnections = new AtomicLong();
    private static AtomicLong totalConnectionsHistory = new AtomicLong();
    private static AtomicLong totalMessages = new AtomicLong();
    private static AtomicLong totalPush = new AtomicLong();
    private static AtomicLong totalPushFailed = new AtomicLong();

    public static long incrMessageTotal() {
        return totalMessages.getAndIncrement();
    }

    public static long incrPushTotal(boolean ok) {
        if (ok) {
            return totalPush.getAndIncrement();
        } else {
            return totalPushFailed.getAndIncrement();
        }
    }

    public static long getTotalConnections() {
        return totalConnections.get();
    }

    public static void incrConnection(){
        totalConnections.incrementAndGet();
        totalConnectionsHistory.incrementAndGet();
    }
    public static void decrConnection(){
        totalConnections.decrementAndGet();
    }
    public static void updateConnection(int count){
        totalConnections.addAndGet(count);
    }

    public static class MonitorThread extends Thread {

        public void run() {
            while (true) {
                logger.info("totalConnections: " + totalConnections.get());
                logger.info("totalConnectionsHistory: " + totalConnectionsHistory.get());
                logger.info("totalMessages: " + totalMessages.get());
                logger.info("totalPush: " + totalPush.get());
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void log(){
        new MonitorThread().start();
    }
}
