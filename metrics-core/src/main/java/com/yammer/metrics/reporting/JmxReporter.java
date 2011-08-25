package com.yammer.metrics.reporting;

import com.yammer.metrics.core.*;

import javax.management.*;
import java.lang.management.ManagementFactory;
import java.util.*;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A reporter which exposes application metric as JMX MBeans.
 */
public class JmxReporter implements Runnable {
    private final ScheduledExecutorService tickThread;
    private final MetricsRegistry metricsRegistry;
    private final Map<Class<?>, MetricToMBeanConverter<?>> converters;
    private final Map<MetricName, MetricMBean> beans;
    private final MBeanServer server;

    private static JmxReporter INSTANCE;
    public static final void startDefault(MetricsRegistry defaultMetricsRegistry) {
        INSTANCE = new JmxReporter(defaultMetricsRegistry);
        INSTANCE.start();
    }

    public JmxReporter(MetricsRegistry metricsRegistry) {
        this.tickThread = metricsRegistry.threadPools().newScheduledThreadPool(1, "jmx-reporter");
        this.metricsRegistry = metricsRegistry;
        this.beans = new HashMap<MetricName, MetricMBean>(metricsRegistry.allMetrics().size());
        this.server = ManagementFactory.getPlatformMBeanServer();
        this.converters = new HashMap<Class<?>, MetricToMBeanConverter<?>>();
        this.converters.put(CounterMetric.class, new CounterMetricToMBeanConverter());
        this.converters.put(GaugeMetric.class, new GaugeMetricToMBeanConverter());
        this.converters.put(MeterMetric.class, new MeterMetricToMBeanConverter());
        this.converters.put(TimerMetric.class, new TimerMetricToMBeanConverter());
        this.converters.put(HistogramMetric.class, new HistogramMetricToMBeanConverter());
    }

    public <T extends Metric> void registerConverter(Class<T> metricType, MetricToMBeanConverter<T> converter) {
        converters.put(metricType, converter);
    }
    
    public void start() {
        tickThread.scheduleAtFixedRate(this, 0, 1, TimeUnit.MINUTES);
        // then schedule the tick thread every 100ms for the next second so
        // as to pick up the initialization of most metrics (in the first 1s of
        // the application lifecycle) w/o incurring a high penalty later on
        for (int i = 1; i <= 9; i++) {
            tickThread.schedule(this, i * 100, TimeUnit.MILLISECONDS);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void run() {
        final Set<MetricName> newMetrics = new HashSet<MetricName>(metricsRegistry.allMetrics().keySet());
        newMetrics.removeAll(beans.keySet());

        for (MetricName name : newMetrics) {
            final Metric metric = metricsRegistry.allMetrics().get(name);
            if (metric != null) {
                try {
                    final ObjectName objectName = new ObjectName(name.getMBeanName());
                    final MetricToMBeanConverter converter = findConverter(metric.getClass());

                    if (converter != null) {
                        registerBean(name, converter.createMBean(metric, objectName), objectName);
                    }
                } catch (Exception ignored) {
                }
            }
        }
    }

    private MetricToMBeanConverter<?> findConverter(Class<?> type) {
        MetricToMBeanConverter<?> converter = null;

        if (type != null) {
            converter = converters.get(type);
            if (converter == null) {
                converter = findConverter(type.getSuperclass());
            }
            if (converter == null) {
                for (Class<?> baseInterface : type.getInterfaces()) {
                    converter = findConverter(baseInterface);
                    if (converter != null) {
                        break;
                    }
                }
            }
        }
        return converter;
    }
    
    private void registerBean(MetricName name, MetricMBean bean, ObjectName objectName) throws MBeanRegistrationException, InstanceAlreadyExistsException, NotCompliantMBeanException {
        beans.put(name, bean);
        server.registerMBean(bean, objectName);
    }
}
