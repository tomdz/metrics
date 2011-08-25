package com.yammer.metrics.reporting;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.management.ObjectName;

import com.yammer.metrics.core.TimerMetric;
import com.yammer.metrics.reporting.HistogramMetricToMBeanConverter.HistogramMBean;
import com.yammer.metrics.reporting.MeterMetricToMBeanConverter.Meter;
import com.yammer.metrics.reporting.MeterMetricToMBeanConverter.MeterMBean;

public class TimerMetricToMBeanConverter implements MetricToMBeanConverter<TimerMetric>
{
    public static interface TimerMBean extends MeterMBean, HistogramMBean {
        public TimeUnit getLatencyUnit();
    }

    public static class Timer extends Meter implements TimerMBean {
        private final TimerMetric metric;

        public Timer(TimerMetric metric, ObjectName objectName) {
            super(metric, objectName);
            this.metric = metric;
        }

        @Override
        public double get50thPercentile() {
            return metric.percentiles(0.5)[0];
        }

        @Override
        public TimeUnit getLatencyUnit() {
            return metric.durationUnit();
        }

        @Override
        public double getMin() {
            return metric.min();
        }

        @Override
        public double getMax() {
            return metric.max();
        }

        @Override
        public double getMean() {
            return metric.mean();
        }

        @Override
        public double getStdDev() {
            return metric.stdDev();
        }

        @Override
        public double get75thPercentile() {
            return metric.percentiles(0.75)[0];
        }

        @Override
        public double get95thPercentile() {
            return metric.percentiles(0.95)[0];
        }

        @Override
        public double get98thPercentile() {
            return metric.percentiles(0.98)[0];
        }

        @Override
        public double get99thPercentile() {
            return metric.percentiles(0.99)[0];
        }

        @Override
        public double get999thPercentile() {
            return metric.percentiles(0.999)[0];
        }

        @Override
        public List<?> values() {
            return metric.values();
        }
    }

    @Override
    public Timer createMBean(TimerMetric metric, ObjectName objectName) {
        return new Timer(metric, objectName);
    }
}
