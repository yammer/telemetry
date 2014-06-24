package com.yammer.metrics.core;

import com.yammer.telemetry.tracing.Span;
import com.yammer.telemetry.tracing.SpanHelper;

@SuppressWarnings("UnusedDeclaration")
public class InstrumentedCounter extends Counter implements MetricNameAware {
    private MetricName metricName;

    @Override
    public void inc(long n) {
        for (Span span : SpanHelper.currentSpan().asSet()) {
            span.addAnnotation("Inc Counter: " + metricName, String.valueOf(n));
        }
        super.inc(n);
    }

    @Override
    public void dec(long n) {
        for (Span span : SpanHelper.currentSpan().asSet()) {
            span.addAnnotation("Dec Counter: " + metricName, String.valueOf(n));
        }
        super.dec(n);
    }

    @Override
    public void clear() {
        for (Span span : SpanHelper.currentSpan().asSet()) {
            span.addAnnotation("Cleared Counter: " + metricName);
        }
        super.clear();
    }

    @Override
    public MetricName getMetricName() {
        return metricName;
    }

    @Override
    public void setMetricName(MetricName metricName) {
        this.metricName = metricName;
    }
}
