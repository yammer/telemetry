package com.yammer.metrics.core;

import com.yammer.telemetry.tracing.Span;

public class InstrumentedTimerContext extends TimerContext {
    private final Span span;

    public InstrumentedTimerContext(InstrumentedTimer timer, Clock clock) {
        super(timer, clock);
        span = Span.startSpan("Timer: " + timer.getMetricName());
    }

    @Override
    public void stop() {
        try {
            super.stop();
        } finally {
            span.end();
        }
    }
}
