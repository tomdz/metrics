package com.yammer.metrics.reporting;

import javax.management.ObjectName;

import com.yammer.metrics.core.GaugeMetric;

public class GaugeMetricToMBeanConverter implements MetricToMBeanConverter<GaugeMetric<?>>
{
    public static interface GaugeMBean extends MetricMBean {
        public Object getValue();
    }

    public static class Gauge extends AbstractBean implements GaugeMBean {
        private final GaugeMetric<?> metric;

        public Gauge(GaugeMetric<?> metric, ObjectName objectName) {
            super(objectName);
            this.metric = metric;
        }

        @Override
        public Object getValue() {
            return metric.value();
        }
    }

    @Override
    public Gauge createMBean(GaugeMetric<?> metric, ObjectName objectName) {
        return new Gauge(metric, objectName);
    }
}
