package com.yammer.telemetry.tracing;

public class DisabledSpan extends Span {
    public DisabledSpan() {
        super(null, null, null, null, SpanHelper.nowInNanoseconds(), System.nanoTime(), TraceLevel.OFF);
    }

    @Override
    public void addAnnotation(String name) {
        // noop
    }

    @Override
    public void addAnnotation(String name, String message) {
        // noop
    }

    @Override
    protected void afterClose() {
        // noop
    }
}
