package com.yammer.metrics.reporting;

import java.util.List;

import javax.management.ObjectName;

import com.yammer.metrics.core.HistogramMetric;

public class HistogramMetricToMBeanConverter implements MetricToMBeanConverter<HistogramMetric>
{
    public static interface HistogramMBean extends MetricMBean {
        public long getCount();
        public double getMin();
        public double getMax();
        public double getMean();
        public double getStdDev();
        public double get50thPercentile();
        public double get75thPercentile();
        public double get95thPercentile();
        public double get98thPercentile();
        public double get99thPercentile();
        public double get999thPercentile();
        public List<?> values();
    }

    public static class Histogram implements HistogramMBean {
        private final ObjectName objectName;
        private final HistogramMetric metric;

        public Histogram(HistogramMetric metric, ObjectName objectName) {
            this.metric = metric;
            this.objectName = objectName;
        }

        @Override
        public ObjectName objectName() {
            return objectName;
        }

        @Override
        public double get50thPercentile() {
            return metric.percentiles(0.5)[0];
        }

        @Override
        public long getCount() {
            return metric.count();
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
    public Histogram createMBean(HistogramMetric metric, ObjectName objectName) {
        return new Histogram(metric, objectName);
    }
}
