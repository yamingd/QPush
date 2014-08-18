package com.whosbean.qpush.core;

import com.codahale.metrics.JmxReporter;
import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Slf4jReporter;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Created by yaming_deng on 14-8-12.
 */
public class MetricBuilder {

    public static final MetricRegistry registry = new MetricRegistry();

    static {
        final JmxReporter jmxReporter = JmxReporter.forRegistry(registry).build();
        jmxReporter.start();

        final Slf4jReporter slf4jReporter = Slf4jReporter.forRegistry(registry)
                .outputTo(LoggerFactory.getLogger("com.whosbean.qpush"))
                .convertRatesTo(TimeUnit.SECONDS)
                .convertDurationsTo(TimeUnit.MILLISECONDS)
                .build();
        slf4jReporter.start(1, TimeUnit.MINUTES);

    }

    public static final Meter jdbcUpdateMeter = MetricBuilder.registry.meter(MetricRegistry.name("qpush.jdbc", "update"));
    /**
     * 所有推送
     */
    public static final Meter pushMeter = MetricBuilder.registry.meter(MetricRegistry.name("qpush.gateway.push", "all"));
    /**
     * 单点推送
     */
    public static final Meter pushSingleMeter = MetricBuilder.registry.meter(MetricRegistry.name("qpush.gateway.push", "single"));
    /**
     * 广播推送
     */
    public static final Meter boradcastMeter = MetricBuilder.registry.meter(MetricRegistry.name("qpush.gateway.push", "broadcast"));

    /**
     * 客户端请求
     */
    public static final Meter requestMeter = MetricBuilder.registry.meter(MetricRegistry.name("qpush.gateway", "requests"));

    /**
     * 消息接收
     */
    public static final Meter recvMeter = MetricBuilder.registry.meter(MetricRegistry.name("qpush.publisher", "requests"));

    /**
     * 客户端类型请求统计
     */
    public static final Meter clientAndroidMeter = MetricBuilder.registry.meter(MetricRegistry.name("qpush.client", "android"));
    public static final Meter clientIOSMeter = MetricBuilder.registry.meter(MetricRegistry.name("qpush.client", "ios"));
}
