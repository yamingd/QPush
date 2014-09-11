package com.whosbean.qpush.gateway;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by yaming_deng on 14-9-10.
 */
public class SentProgress {

    private CountDownLatch countDownLatch;
    private AtomicInteger success = new AtomicInteger(0);
    private AtomicInteger failed = new AtomicInteger(0);

    public SentProgress(int total) {
        this.countDownLatch = new CountDownLatch(total);
    }

    public AtomicInteger getFailed() {
        return failed;
    }

    public AtomicInteger getSuccess() {
        return success;
    }

    public CountDownLatch getCountDownLatch() {
        return countDownLatch;
    }

    public void incrSuccess(){
        success.incrementAndGet();
        countDownLatch.countDown();
    }

    public void incrFailed(){
        failed.incrementAndGet();
        countDownLatch.countDown();
    }

    public int getTotal(){
        return this.success.get() + this.failed.get();
    }

    @Override
    public String toString() {
        return "SentProgress{" +
                ", success=" + success.get() +
                ", failed=" + failed.get() +
                '}';
    }
}
