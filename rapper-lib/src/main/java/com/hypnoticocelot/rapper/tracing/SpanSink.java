package com.hypnoticocelot.rapper.tracing;

public interface SpanSink {

    void record(Span span);
}
