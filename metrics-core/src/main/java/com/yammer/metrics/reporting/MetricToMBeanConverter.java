package com.yammer.metrics.reporting;

import javax.management.ObjectName;

import com.yammer.metrics.core.Metric;

public interface MetricToMBeanConverter<MetricType extends Metric>
{
    MetricMBean createMBean(MetricType metric, ObjectName objectName);
}
