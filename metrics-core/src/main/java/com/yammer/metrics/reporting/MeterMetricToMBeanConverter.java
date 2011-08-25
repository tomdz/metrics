package com.yammer.metrics.reporting;

import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import com.yammer.metrics.core.MeterMetric;
import com.yammer.metrics.core.Metered;

public class MeterMetricToMBeanConverter implements MetricToMBeanConverter<MeterMetric>
{
    public static interface MeterMBean extends MetricMBean {
        public long getCount();
        public String getEventType();
        public TimeUnit getRateUnit();
        public double getMeanRate();
        public double getOneMinuteRate();
        public double getFiveMinuteRate();
        public double getFifteenMinuteRate();
    }

    public static class Meter extends AbstractBean implements MeterMBean {
        private final Metered metric;

        public Meter(Metered metric, ObjectName objectName) {
            super(objectName);
            this.metric = metric;
        }

        @Override
        public long getCount() {
            return metric.count();
        }

        @Override
        public String getEventType() {
            return metric.eventType();
        }

        @Override
        public TimeUnit getRateUnit() {
            return metric.rateUnit();
        }

        @Override
        public double getMeanRate() {
            return metric.meanRate();
        }

        @Override
        public double getOneMinuteRate() {
            return metric.oneMinuteRate();
        }

        @Override
        public double getFiveMinuteRate() {
            return metric.fiveMinuteRate();
        }

        @Override
        public double getFifteenMinuteRate() {
            return metric.fifteenMinuteRate();
        }
    }

    @Override
    public Meter createMBean(MeterMetric metric, ObjectName objectName) {
        return new Meter(metric, objectName);
    }
}
