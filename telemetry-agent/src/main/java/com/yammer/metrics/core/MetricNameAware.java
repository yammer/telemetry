package com.yammer.metrics.core;

public interface MetricNameAware {
    MetricName getMetricName();
    void setMetricName(MetricName metricName);
}
