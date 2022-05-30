package org.monkey.mmq.metrics;

import com.codahale.metrics.Counter;
import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
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

    private final MetricRegistry metricRegistry;
    private final Counter incomingMessageCounter;
    private final Counter outgoingMessageCounter;

    private final Counter incomingConnectCounter;

    private final Counter incomingPublishCounter;
    private final Counter outgoingPublishCounter;

    private final Counter subscriptionCounter;

    public MetricsHolder() {
        this.metricRegistry = new MetricRegistry();

        incomingMessageCounter = metricRegistry.counter(INCOMING_MESSAGE_COUNT.name());
        outgoingMessageCounter = metricRegistry.counter(OUTGOING_MESSAGE_COUNT.name());

        incomingConnectCounter = metricRegistry.counter(INCOMING_CONNECT_COUNT.name());

        incomingPublishCounter = metricRegistry.counter(INCOMING_PUBLISH_COUNT.name());
        outgoingPublishCounter = metricRegistry.counter(OUTGOING_PUBLISH_COUNT.name());

        subscriptionCounter = metricRegistry.counter(SUBSCRIPTIONS_CURRENT.name());

    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public Counter getIncomingMessageCounter() {
        return incomingMessageCounter;
    }

    public Counter getOutgoingMessageCounter() {
        return outgoingMessageCounter;
    }

    public Counter getIncomingConnectCounter() {
        return incomingConnectCounter;
    }

    public Counter getIncomingPublishCounter() {
        return incomingPublishCounter;
    }

    public Counter getOutgoingPublishCounter() {
        return outgoingPublishCounter;
    }

    public Counter getSubscriptionCounter() {
        return subscriptionCounter;
    }
}
