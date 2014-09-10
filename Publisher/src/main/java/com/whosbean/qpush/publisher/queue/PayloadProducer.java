package com.whosbean.qpush.publisher.queue;

import com.lmax.disruptor.RingBuffer;
import com.whosbean.qpush.client.PayloadMessage;
import com.whosbean.qpush.core.MetricBuilder;

/**
 * Created by yaming_deng on 14-8-8.
 */
public class PayloadProducer {

    private final RingBuffer<JsonMessage> ringBuffer;

    public PayloadProducer(RingBuffer<JsonMessage> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void push(PayloadMessage data) {
        MetricBuilder.recvMeter.mark();

        long sequence = ringBuffer.next();  // Grab the next sequence
        try {

            JsonMessage event = ringBuffer.get(sequence); // Get the entry in the Disruptor
            // for the sequence
            event.setBody(data);

        }catch (Exception ex){
            ex.printStackTrace();
        }
        finally {
            ringBuffer.publish(sequence);
        }


    }
}
