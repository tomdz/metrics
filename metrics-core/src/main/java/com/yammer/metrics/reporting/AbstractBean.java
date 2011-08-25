package com.yammer.metrics.reporting;

import javax.management.ObjectName;

public abstract class AbstractBean implements MetricMBean {
    private final ObjectName objectName;

    protected AbstractBean(ObjectName objectName) {
        this.objectName = objectName;
    }

    @Override
    public ObjectName objectName() {
        return objectName;
    }
}