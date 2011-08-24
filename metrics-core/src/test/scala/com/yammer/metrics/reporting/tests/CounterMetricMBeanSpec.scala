package com.yammer.metrics.reporting.tests

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import com.yammer.metrics.core.{CounterMetric, MetricName, MetricsRegistry}
import com.yammer.metrics.reporting.JmxReporter
import java.lang.management.ManagementFactory
import javax.management.ObjectName

class CounterMetricMBeanSpec extends Spec {
  class `A counter metric` {
    val metricsRegistry = new MetricsRegistry
    val metricName = new MetricName("foo", "bar", "counter")
    val counter = metricsRegistry.newCounter(metricName)
    val jmxReporter = new JmxReporter(metricsRegistry)
    val mbeanServer = ManagementFactory.getPlatformMBeanServer()
    val objectName = new ObjectName(metricName.getMBeanName())

    @test def `counter mbean exported` = {
      counter.inc()

      jmxReporter.run()

      val mbeanInfo = mbeanServer.getMBeanInfo(objectName)

      mbeanInfo.must(be(notNull))
      try {
        val attributeNames = mbeanInfo.getAttributes().map(_.getName()).toSet
        val operationNames = mbeanInfo.getOperations().map(_.getName()).toSet

        attributeNames.must(be(Set("Count")))
        operationNames.must(be(Set("objectName")))

        val count = mbeanServer.getAttribute(objectName, "Count")

        count.asInstanceOf[Long].must(be(1l))
      }
      finally {
        mbeanServer.unregisterMBean(objectName)
      }
    }
  }
}
