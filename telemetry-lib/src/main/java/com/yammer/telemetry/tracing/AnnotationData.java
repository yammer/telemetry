package com.yammer.telemetry.tracing;

public interface AnnotationData {
    long getLoggedAt();

    String getName();

    String getMessage();
}
