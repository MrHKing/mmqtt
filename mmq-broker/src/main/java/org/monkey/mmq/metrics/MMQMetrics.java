package org.monkey.mmq.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;

/**
 * @ClassNameMMQMetrics
 * @Description
 * @Author Solley
 * @Date2022/5/30 15:12
 * @Version V1.0
 **/
public class MMQMetrics {
    /**
     * represents a {@link Counter}, which counts every incoming MQTT message
     *
     * @since 3.0
     */
    public static final MMQMetric<Counter> INCOMING_MESSAGE_COUNT =
            MMQMetric.valueOf("org.monkey.mmq.messages.incoming.total.count", Counter.class);

    /**
     * represents a {@link Counter}, which counts every outgoing MQTT message
     *
     * @since 3.0
     */
    public static final MMQMetric<Counter> OUTGOING_MESSAGE_COUNT =
            MMQMetric.valueOf("org.monkey.mmq.messages.outgoing.total.count", Counter.class);

    /**
     * represents a {@link Counter}, which counts every incoming MQTT CONNECT messages
     *
     * @since 3.0
     */
    public static final MMQMetric<Counter> INCOMING_CONNECT_COUNT =
            MMQMetric.valueOf("org.monkey.mmq.messages.incoming.connect.count", Counter.class);

    /**
     * represents a {@link Counter}, which counts every incoming MQTT PUBLISH messages
     *
     * @since 3.0
     */
    public static final MMQMetric<Counter> INCOMING_PUBLISH_COUNT =
            MMQMetric.valueOf("org.monkey.mmq.messages.incoming.publish.count", Counter.class);

    /**
     * represents a {@link Counter}, which counts every outgoing MQTT PUBLISH messages
     *
     * @since 3.0
     */
    public static final MMQMetric<Counter> OUTGOING_PUBLISH_COUNT =
            MMQMetric.valueOf("org.monkey.mmq.messages.outgoing.publish.count", Counter.class);

    /**
     * represents a {@link Counter}, which measures the current count of subscriptions
     *
     * @since 3.0
     */
    public static final MMQMetric<Counter> SUBSCRIPTIONS_CURRENT =
            MMQMetric.valueOf("org.monkey.mmq.subscriptions.overall.current", Counter.class);

    /**
     * represents a {@link Gauge}, which holds the total amount of read bytes
     *
     * @since 3.0
     */
    public static final MMQMetric<Gauge<Number>> BYTES_READ_TOTAL =
            MMQMetric.gaugeValue("org.monkey.mmq.networking.bytes.read.total");

    /**
     * represents a {@link Gauge}, which holds total of written bytes
     *
     * @since 3.0
     */
    public static final MMQMetric<Gauge<Number>> BYTES_WRITE_TOTAL =
            MMQMetric.gaugeValue("org.monkey.mmq.networking.bytes.write.total");
}
