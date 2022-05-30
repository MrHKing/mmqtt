package org.monkey.mmq.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.sun.istack.internal.NotNull;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.LongAdder;

import static org.monkey.mmq.metrics.MMQMetrics.*;

/**
 * @ClassNameMetricsHolder
 * @Description
 * @Author Solley
 * @Date2022/5/30 15:07
 * @Version V1.0
 **/
@Component
public class MetricsHolder {

    private final @NotNull MetricRegistry metricRegistry;
    private final @NotNull Counter incomingMessageCounter;
    private final @NotNull Counter outgoingMessageCounter;

    private final @NotNull Counter incomingConnectCounter;

    private final @NotNull Counter incomingPublishCounter;
    private final @NotNull Counter outgoingPublishCounter;

    private final @NotNull Counter subscriptionCounter;

    public MetricsHolder() {
        this.metricRegistry = new MetricRegistry();

        incomingMessageCounter = metricRegistry.counter(INCOMING_MESSAGE_COUNT.name());
        outgoingMessageCounter = metricRegistry.counter(OUTGOING_MESSAGE_COUNT.name());

        incomingConnectCounter = metricRegistry.counter(INCOMING_CONNECT_COUNT.name());

        incomingPublishCounter = metricRegistry.counter(INCOMING_PUBLISH_COUNT.name());
        outgoingPublishCounter = metricRegistry.counter(OUTGOING_PUBLISH_COUNT.name());

        subscriptionCounter = metricRegistry.counter(SUBSCRIPTIONS_CURRENT.name());

    }

    public @NotNull MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public @NotNull Counter getIncomingMessageCounter() {
        return incomingMessageCounter;
    }

    public @NotNull Counter getOutgoingMessageCounter() {
        return outgoingMessageCounter;
    }

    public @NotNull Counter getIncomingConnectCounter() {
        return incomingConnectCounter;
    }

    public @NotNull Counter getIncomingPublishCounter() {
        return incomingPublishCounter;
    }

    public @NotNull Counter getOutgoingPublishCounter() {
        return outgoingPublishCounter;
    }

    public @NotNull Counter getSubscriptionCounter() {
        return subscriptionCounter;
    }
}
