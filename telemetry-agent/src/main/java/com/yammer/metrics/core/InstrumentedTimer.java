package com.yammer.metrics.core;

import com.yammer.telemetry.tracing.Span;

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
        try (Span ignored = Span.startSpan("Timer: " + metricName)) {
            return super.time(event);
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
