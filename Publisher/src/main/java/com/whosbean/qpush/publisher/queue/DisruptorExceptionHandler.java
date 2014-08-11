package com.whosbean.qpush.publisher.queue;

import com.lmax.disruptor.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yaming_deng on 14-8-8.
 */
public class DisruptorExceptionHandler implements ExceptionHandler {

    protected static Logger logger = LoggerFactory.getLogger(DisruptorExceptionHandler.class);

    @Override
    public void handleEventException(Throwable ex, long sequence, Object event) {
        logger.error("ERROR handleEventException. Sequence:"+sequence+", event:"+event, ex);
    }

    @Override
    public void handleOnStartException(Throwable ex) {
        logger.error("handleOnStartException", ex);
    }

    @Override
    public void handleOnShutdownException(Throwable ex) {
        logger.error("handleOnShutdownException", ex);
    }
}
