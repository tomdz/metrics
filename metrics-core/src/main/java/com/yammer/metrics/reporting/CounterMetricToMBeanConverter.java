package com.yammer.metrics.reporting;

import javax.management.ObjectName;

import com.yammer.metrics.core.CounterMetric;

public class CounterMetricToMBeanConverter implements MetricToMBeanConverter<CounterMetric>
{
    public static interface CounterMBean extends MetricMBean {
        public long getCount();
    }

    public static class Counter extends AbstractBean implements CounterMBean {
        private final CounterMetric metric;

        public Counter(CounterMetric metric, ObjectName objectName) {
            super(objectName);
            this.metric = metric;
        }

        @Override
        public long getCount() {
            return metric.count();
        }
    }

    @Override
    public Counter createMBean(CounterMetric metric, ObjectName objectName) {
        return new Counter(metric, objectName);
    }
}
