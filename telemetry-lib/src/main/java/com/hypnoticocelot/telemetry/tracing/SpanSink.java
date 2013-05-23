package com.hypnoticocelot.telemetry.tracing;

public interface SpanSink {

    void record(Span span);
}
