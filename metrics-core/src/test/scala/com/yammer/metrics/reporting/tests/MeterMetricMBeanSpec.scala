package com.yammer.metrics.reporting.tests

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import com.yammer.metrics.core.{HistogramMetric, MetricName, MetricsRegistry}
import com.yammer.metrics.reporting.JmxReporter
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import javax.management.ObjectName

class MeterMetricMBeanSpec extends Spec {
  class `A meter metric` {
    val metricsRegistry = new MetricsRegistry
    val metricName = new MetricName("foo", "bar", "meter")
    val meter = metricsRegistry.newMeter(metricName, "thangs", TimeUnit.SECONDS)
    val jmxReporter = new JmxReporter(metricsRegistry)
    val mbeanServer = ManagementFactory.getPlatformMBeanServer()

    @test def `meter mbean exported` = {
      meter.mark(3)

      jmxReporter.run()

      val objectName = new ObjectName(metricName.getMBeanName())
      val mbeanInfo = mbeanServer.getMBeanInfo(objectName)

      mbeanInfo.must(be(notNull))
      try {
        val attributeNames = mbeanInfo.getAttributes().map(_.getName()).toSet
        val operationNames = mbeanInfo.getOperations().map(_.getName()).toSet

        attributeNames.must(be(Set("Count", "EventType", "RateUnit", "MeanRate", "OneMinuteRate", "FiveMinuteRate", "FifteenMinuteRate")))
        operationNames.must(be(Set("objectName")))

        val value = mbeanServer.getAttribute(objectName, "Count")

        value.asInstanceOf[Long].must(be(3l))
      }
      finally {
        mbeanServer.unregisterMBean(objectName)
      }
    }
  }
}
