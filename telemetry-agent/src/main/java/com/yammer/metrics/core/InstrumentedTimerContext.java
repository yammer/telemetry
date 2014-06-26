package com.yammer.metrics.core;

import com.google.common.base.Optional;
import com.yammer.telemetry.tracing.Span;
import com.yammer.telemetry.tracing.SpanHelper;

public class InstrumentedTimerContext extends TimerContext {
    private final Optional<Span> currentSpan;
    private final MetricName metricName;

    public InstrumentedTimerContext(InstrumentedTimer timer, Clock clock) {
        super(timer, clock);
        currentSpan = SpanHelper.currentSpan();
        metricName = timer.getMetricName();
        for (Span span : SpanHelper.currentSpan().asSet()) {
            span.addAnnotation("Start Timer", String.valueOf(metricName));
        }
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } finally {
            for (Span span : currentSpan.asSet()) {
                span.addAnnotation("Stop Timer", String.valueOf(metricName));
            }
        }
    }
}
