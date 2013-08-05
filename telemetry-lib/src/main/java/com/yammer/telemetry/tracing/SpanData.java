package com.yammer.telemetry.tracing;

import java.util.UUID;

public interface SpanData {
    UUID getTraceId();

    UUID getId();

    UUID getParentId();

    String getName();

    long getStartTimeNanos();

    long getDuration();
}
