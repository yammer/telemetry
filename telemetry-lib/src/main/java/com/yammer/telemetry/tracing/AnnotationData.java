package com.yammer.telemetry.tracing;

public interface AnnotationData {
    long getStartTimeNanos();

    String getName();

    String getMessage();
}
