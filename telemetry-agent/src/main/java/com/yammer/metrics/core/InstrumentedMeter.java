package com.yammer.metrics.core;

import com.yammer.telemetry.tracing.Span;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class InstrumentedMeter extends Meter implements MetricNameAware {
    private MetricName metricName;

    /**
     * Creates a new {@link com.yammer.metrics.core.Meter}.
     *
     * @param tickThread background thread for updating the rates
     * @param eventType  the plural name of the event the meter is measuring (e.g., {@code
     *                   "requests"})
     * @param rateUnit   the rate unit of the new meter
     * @param clock      the clock to use for the meter ticks
     */
    InstrumentedMeter(ScheduledExecutorService tickThread, String eventType, TimeUnit rateUnit, Clock clock) {
        super(tickThread, eventType, rateUnit, clock);
    }

    @Override
    public void mark(long n) {
        for (Span span : Span.currentSpan().asSet()) {
            span.addAnnotation("Mark Meter: " + metricName, String.valueOf(n));
        }
        super.mark(n);
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
