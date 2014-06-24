package com.yammer.metrics.core;

import com.google.common.base.Optional;
import com.yammer.telemetry.tracing.Span;
import com.yammer.telemetry.tracing.SpanHelper;

import java.util.concurrent.Callable;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InstrumentedTimer extends Timer implements MetricNameAware {
    private final Clock clock;
    private MetricName metricName;

    InstrumentedTimer(ScheduledExecutorService tickThread, TimeUnit durationUnit, TimeUnit rateUnit, Clock clock) {
        super(tickThread, durationUnit, rateUnit, clock);
        this.clock = clock;
    }

    @Override
    public <T> T time(Callable<T> event) throws Exception {
        final Optional<Span> currentSpan = SpanHelper.currentSpan();
        try {
            for (Span span : currentSpan.asSet()) {
                span.addAnnotation("Start Timer", String.valueOf(metricName));
            }

            return super.time(event);
        } finally {
            for (Span span : currentSpan.asSet()) {
                span.addAnnotation("Stop Timer", String.valueOf(metricName));
            }
        }
    }

    @Override
    public TimerContext time() {
        return new InstrumentedTimerContext(this, clock);
    }

    public MetricName getMetricName() {
        return metricName;
    }

    public void setMetricName(MetricName metricName) { this.metricName = metricName; }
}
