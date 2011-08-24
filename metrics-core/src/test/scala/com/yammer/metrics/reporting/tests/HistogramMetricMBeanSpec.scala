package com.yammer.metrics.reporting.tests

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import com.yammer.metrics.core.{HistogramMetric, MetricName, MetricsRegistry}
import com.yammer.metrics.reporting.JmxReporter
import java.lang.management.ManagementFactory
import javax.management.ObjectName

class HistogramMetricMBeanSpec extends Spec {
  class `A histogram metric` {
    val metricsRegistry = new MetricsRegistry
    val metricName = new MetricName("foo", "bar", "histogram")
    val histogram = metricsRegistry.newHistogram(metricName, false)
    val jmxReporter = new JmxReporter(metricsRegistry)
    val mbeanServer = ManagementFactory.getPlatformMBeanServer()

    @test def `histogram mbean exported` = {
      (1 to 10000).foreach(histogram.update)

      jmxReporter.run()

      val objectName = new ObjectName(metricName.getMBeanName())
      val mbeanInfo = mbeanServer.getMBeanInfo(objectName)

      mbeanInfo.must(be(notNull))
      try {
        val attributeNames = mbeanInfo.getAttributes().map(_.getName()).toSet
        val operationNames = mbeanInfo.getOperations().map(_.getName()).toSet

        attributeNames.must(be(Set("Count", "Min", "Max", "Mean", "StdDev", "50thPercentile", "75thPercentile", "95thPercentile", "98thPercentile", "99thPercentile", "999thPercentile")))
        operationNames.must(be(Set("objectName", "values")))

        val value = mbeanServer.getAttribute(objectName, "Count")

        value.asInstanceOf[Long].must(be(10000l))
      }
      finally {
        mbeanServer.unregisterMBean(objectName)
      }
    }
  }
}
