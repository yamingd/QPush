package com.whosbean.qpush.publisher.queue;

import com.lmax.disruptor.RingBuffer;

/**
 * Created by yaming_deng on 14-8-8.
 */
public class PayloadProducer {

    private final RingBuffer<JsonMessage> ringBuffer;

    public PayloadProducer(RingBuffer<JsonMessage> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void push(String data) {
        long sequence = ringBuffer.next();  // Grab the next sequence
        try {
            JsonMessage event = ringBuffer.get(sequence); // Get the entry in the Disruptor
            // for the sequence
            event.setBody(data);

        } finally {
            ringBuffer.publish(sequence);
        }
    }
}
