package com.yammer.telemetry.service.models;

import com.yammer.telemetry.tracing.AnnotationData;

public class BeanAnnotationData implements AnnotationData {
    private long loggedAt;
    private String name;
    private String message;

    private BeanAnnotationData() { }

    @Override
    public long getLoggedAt() {
        return loggedAt;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "BeanAnnotationData{" +
                "loggedAt=" + loggedAt +
                ", name='" + name + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
