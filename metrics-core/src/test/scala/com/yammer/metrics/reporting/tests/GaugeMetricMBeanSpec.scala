package com.yammer.metrics.reporting.tests

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import com.yammer.metrics.core.{GaugeMetric, MetricName, MetricsRegistry}
import com.yammer.metrics.reporting.JmxReporter
import java.lang.management.ManagementFactory
import javax.management.ObjectName

class GaugeMetricMBeanSpec extends Spec {
  class `A gauge metric` {
    val metric = new GaugeMetric[String] {
      def value = "woo"
    }
    val metricsRegistry = new MetricsRegistry
    val metricName = new MetricName("foo", "bar", "gauge")
    val gauge = metricsRegistry.newGauge(metricName, metric)
    val jmxReporter = new JmxReporter(metricsRegistry)
    val mbeanServer = ManagementFactory.getPlatformMBeanServer()

    @test def `gauge mbean exported` = {
      jmxReporter.run()

      val objectName = new ObjectName(metricName.getMBeanName())
      val mbeanInfo = mbeanServer.getMBeanInfo(objectName)

      mbeanInfo.must(be(notNull))
      try {
        val attributeNames = mbeanInfo.getAttributes().map(_.getName()).toSet
        val operationNames = mbeanInfo.getOperations().map(_.getName()).toSet

        attributeNames.must(be(Set("Value")))
        operationNames.must(be(Set("objectName")))

        val value = mbeanServer.getAttribute(objectName, "Value")

        value.must(be("woo"))
      }
      finally {
        mbeanServer.unregisterMBean(objectName)
      }
    }
  }
}
