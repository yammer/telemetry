package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;

public interface SpanData {
    long getTraceId();

    long getId();

    Optional<Long> getParentId();

    String getName();

    long getStartTimeNanos();

    long getDuration();
}
