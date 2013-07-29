package com.yammer.telemetry.tracing;

import java.util.Map;
import java.util.UUID;

public interface SpanData {
    UUID getTraceId();

    UUID getId();

    UUID getParentId();

    String getName();

    Map<String, String> getAnnotations();

    long getStartTimeNanos();

    long getDuration();
}
