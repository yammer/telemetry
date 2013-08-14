package com.yammer.telemetry.service.models;

import com.yammer.dropwizard.json.JsonSnakeCase;
import com.yammer.telemetry.tracing.AnnotationData;

@JsonSnakeCase
public class BeanAnnotationData implements AnnotationData {
    private long startTimeNanos;
    private String name;
    private String message;

    private BeanAnnotationData() { }

    @Override
    public long getStartTimeNanos() {
        return startTimeNanos;
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
                "startTimeNanos=" + startTimeNanos +
                ", name='" + name + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
