package com.yammer.telemetry.service.models;

import com.google.common.base.Optional;
import com.yammer.telemetry.tracing.AnnotationData;

public class BeanAnnotationData implements AnnotationData {
    private long startTimeNanos;
    private Optional<Long> duration;
    private String message;

    private BeanAnnotationData() { }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    public Optional<Long> getDuration() {
        return duration;
    }

    public String getMessage() {
        return message;
    }
}
