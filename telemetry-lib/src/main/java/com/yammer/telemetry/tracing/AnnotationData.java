package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;

public interface AnnotationData {
    long getStartTimeNanos();

    Optional<Long> getDuration();

    String getMessage();
}
