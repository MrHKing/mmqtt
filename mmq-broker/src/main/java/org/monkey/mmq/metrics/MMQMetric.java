package org.monkey.mmq.metrics;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @ClassNameMMQMetric
 * @Description
 * @Author Solley
 * @Date2022/5/30 15:14
 * @Version V1.0
 **/
public class MMQMetric<T extends Metric> {
    private final String name;
    private final Class<? extends Metric> clazz;

    private MMQMetric(final String name, final Class<? extends Metric> clazz) {
        this.name = name;
        this.clazz = clazz;
    }

    public static <T extends Metric> MMQMetric<T> valueOf(final String name, final Class<T> metricClass) {
        checkNotNull(name, "Name cannot be null");

        return new MMQMetric<>(name, metricClass);
    }

    public static MMQMetric<Gauge<Number>> gaugeValue(final String name) {
        checkNotNull(name, "Name cannot be null");

        return new MMQMetric<>(name, Gauge.class);
    }

    public String name() {
        return name;
    }

    public Class<? extends Metric> getClazz() {
        return clazz;
    }
}
