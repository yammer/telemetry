package com.hypnoticocelot.telemetry.tracing;

public interface SpanSink {

    void record(SpanData spanData);
}
