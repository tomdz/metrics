package com.yammer.metrics.reporting.tests

import com.codahale.simplespec.Spec
import com.codahale.simplespec.annotation.test
import com.yammer.metrics.core.{HistogramMetric, MetricName, MetricsRegistry}
import com.yammer.metrics.reporting.JmxReporter
import java.lang.management.ManagementFactory
import java.util.concurrent.TimeUnit
import javax.management.ObjectName

class TimerMetricMBeanSpec extends Spec {
  class `A timer metric` {
    val metricsRegistry = new MetricsRegistry
    val metricName = new MetricName("foo", "bar", "timer")
    val timer = metricsRegistry.newTimer(metricName, TimeUnit.MILLISECONDS, TimeUnit.SECONDS)
    val jmxReporter = new JmxReporter(metricsRegistry)
    val mbeanServer = ManagementFactory.getPlatformMBeanServer()

    @test def `timer mbean exported` = {
      timer.update(10, TimeUnit.MILLISECONDS)

      jmxReporter.run()

      val objectName = new ObjectName(metricName.getMBeanName())
      val mbeanInfo = mbeanServer.getMBeanInfo(objectName)

      mbeanInfo.must(be(notNull))
      try {
        val attributeNames = mbeanInfo.getAttributes().map(_.getName()).toSet
        val operationNames = mbeanInfo.getOperations().map(_.getName()).toSet

        attributeNames.must(be(Set("Count", "Min", "Max", "Mean", "StdDev", "50thPercentile", "75thPercentile", "95thPercentile",
                                   "98thPercentile", "99thPercentile", "999thPercentile", "EventType", "RateUnit", "MeanRate",
                                   "OneMinuteRate", "FiveMinuteRate", "FifteenMinuteRate", "LatencyUnit")))
        operationNames.must(be(Set("objectName", "values")))

        val value = mbeanServer.getAttribute(objectName, "Count")

        value.asInstanceOf[Long].must(be(1l))
      }
      finally {
        mbeanServer.unregisterMBean(objectName)
      }
    }
  }
}
