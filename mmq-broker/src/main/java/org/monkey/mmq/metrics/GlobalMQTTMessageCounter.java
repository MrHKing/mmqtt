package org.monkey.mmq.metrics;

import com.codahale.metrics.Gauge;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttMessageType;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

import static io.netty.handler.codec.mqtt.MqttMessageType.*;
import static org.monkey.mmq.metrics.MMQMetrics.BYTES_READ_TOTAL;
import static org.monkey.mmq.metrics.MMQMetrics.BYTES_WRITE_TOTAL;

/**
 * @ClassNameGlobalMQTTMessageCounter
 * @Description
 * @Author Solley
 * @Date2022/5/30 15:18
 * @Version V1.0
 **/
@Component
public class GlobalMQTTMessageCounter {
    private final MetricsHolder metricsHolder;
    private final LongAdder bytesReadTotal;
    private final LongAdder bytesWrittenTotal;

    public GlobalMQTTMessageCounter(final MetricsHolder metricsHolder) {
        this.metricsHolder = metricsHolder;
        this.bytesReadTotal = new LongAdder();
        this.bytesWrittenTotal = new LongAdder();


        metricsHolder.getMetricRegistry().register(BYTES_READ_TOTAL.name(), (Gauge<Long>) bytesReadTotal::longValue);
        metricsHolder.getMetricRegistry().register(BYTES_WRITE_TOTAL.name(), (Gauge<Long>) bytesWrittenTotal::longValue);
    }

    public void countInbound(final MqttMessageType mqttMessageType) {
        metricsHolder.getIncomingMessageCounter().inc();
        if (CONNECT.equals(mqttMessageType)) {
            metricsHolder.getIncomingConnectCounter().inc();
        }
        if (PUBLISH.equals(mqttMessageType)) {
            metricsHolder.getIncomingPublishCounter().inc();
        }
        if (SUBSCRIBE.equals(mqttMessageType)) {
            metricsHolder.getSubscriptionCounter().inc();
        }
    }
    public void countInboundTraffic(final int bytes) {
        bytesReadTotal.add(bytes);
    }

    public void countOutbound(final MqttMessageType mqttMessageType) {
        metricsHolder.getOutgoingMessageCounter().inc();
        if (CONNECT.equals(mqttMessageType)) {
            metricsHolder.getIncomingConnectCounter().dec();
        }
        if (PUBLISH.equals(mqttMessageType)) {
            metricsHolder.getOutgoingPublishCounter().inc();
        }
        if (SUBSCRIBE.equals(mqttMessageType)) {
            metricsHolder.getSubscriptionCounter().dec();
        }
    }

    public void countOutboundTraffic(final int bytes) {
        bytesWrittenTotal.add(bytes);
    }
}
