package com.whosbean.qpush.publisher.queue;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import com.whosbean.qpush.pipe.PayloadQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by yaming_deng on 14-2-14.
 */
@Component
public class DisruptorContext implements InitializingBean, ApplicationContextAware {

    protected static Logger logger = LoggerFactory.getLogger(DisruptorContext.class);

    private Disruptor<JsonMessage> disruptor = null;

    private ExecutorService executor;

    private int bufferSize;

    public static PayloadProducer producer = null;
    public static DisruptorContext instance = null;

    private ApplicationContext applicationContext;

    @Autowired
    @Qualifier("payloadMysqlQueue")
    private PayloadQueue defaultQueue;

    @Autowired
    @Qualifier("appConfig")
    private Properties conf;

    @Override
    public void afterPropertiesSet() throws Exception {
        instance = this;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    public class MonitorThread extends Thread{

        private final RingBuffer<JsonMessage> ringBuffer;

        public MonitorThread(RingBuffer<JsonMessage> ringBuffer) {
            this.ringBuffer = ringBuffer;
        }

        @Override
        public void run() {
            int sleepTime = 5*1000;
            while (true) {
                long count = ringBuffer.remainingCapacity();
                logger.info("Disruptor Queue. Max: " + bufferSize + ", Remain: " + count);
                try {
                    Thread.sleep(sleepTime);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void start(){
        if (disruptor != null){
            return;
        }
        // Executor that will be used to construct new threads for consumers
        executor = Executors.newCachedThreadPool();

        // The factory for the event
        PayloadFactory factory = new PayloadFactory();

        // Specify the size of the ring buffer, must be power of 2.
        bufferSize = Integer.parseInt(conf.getProperty("ringbuffer.size", "16"));

        logger.info("Disruptor Queue. BufferSize=" + bufferSize);

        // Construct the Disruptor
        disruptor = new Disruptor<JsonMessage>(factory, bufferSize,
                executor,
                ProducerType.SINGLE,
                new com.lmax.disruptor.BusySpinWaitStrategy()); //多核

        String beanName = conf.getProperty("payloadQueue", "payloadMysqlQueue");
        PayloadQueue handler = this.applicationContext.getBean(beanName, PayloadQueue.class);
        disruptor.handleEventsWith(new PayloadConsumer(handler));
        disruptor.handleExceptionsWith(new DisruptorExceptionHandler());

        // Start the Disruptor, starts all threads running
        disruptor.start();

        // Get the ring buffer from the Disruptor to be used for publishing.
        RingBuffer<JsonMessage> ringBuffer = disruptor.getRingBuffer();

        producer = new PayloadProducer(ringBuffer);

        new MonitorThread(ringBuffer).start();

        logger.info("Disruptor Queue. started");
    }

    public void stop()
    {
        logger.info("Disruptor Queue. Stopping.");
        disruptor.shutdown();
        executor.shutdown();
        logger.info("Disruptor Queue. Stopped.");
    }

}
