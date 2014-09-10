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
