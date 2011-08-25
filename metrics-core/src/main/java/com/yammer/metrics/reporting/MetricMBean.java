package com.yammer.metrics.reporting;

import javax.management.ObjectName;

public interface MetricMBean {
    public ObjectName objectName();
}