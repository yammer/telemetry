package com.yammer.telemetry.tracing;

public interface SpanSink {

    void record(SpanData spanData);

    void clear();
}
