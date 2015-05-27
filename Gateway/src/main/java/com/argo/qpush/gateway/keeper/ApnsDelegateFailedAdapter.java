package com.argo.qpush.gateway.keeper;

import com.notnoop.apns.ApnsDelegateAdapter;
import com.notnoop.apns.ApnsNotification;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by yaming_deng on 14-9-10.
 */
public class ApnsDelegateFailedAdapter extends ApnsDelegateAdapter {

    protected static Logger logger = LoggerFactory.getLogger(ApnsDelegateFailedAdapter.class);

    @Override
    public void messageSendFailed(ApnsNotification message, Throwable e) {
        logger.error("Push Failed. {}" + message, e);
    }
}
