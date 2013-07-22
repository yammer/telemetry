package com.yammer.telemetry.tracing;

import java.util.UUID;

public interface SpanData {
    UUID getTraceId();

    UUID getId();

    UUID getParentId();

    SpanInfo getInfo();

    long getStartTimeNanos();

    long getDuration();
}
