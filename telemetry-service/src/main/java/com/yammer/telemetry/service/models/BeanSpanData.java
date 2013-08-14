package com.yammer.telemetry.service.models;

import com.google.common.base.Optional;
import com.yammer.dropwizard.json.JsonSnakeCase;
import com.yammer.telemetry.tracing.SpanData;

@JsonSnakeCase
public class BeanSpanData implements SpanData {
    private long traceId;
    private long id;
    private Optional<Long> parentId;
    private String name;
    private long startTimeNanos;
    private long duration;

    private BeanSpanData() { }

    @Override
    public long getTraceId() {
        return traceId;
    }

    @Override
    public long getId() {
        return id;
    }

    @Override
    public Optional<Long> getParentId() {
        return parentId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "BeanSpanData{" +
                "traceId=" + traceId +
                ", id=" + id +
                ", parentId=" + parentId +
                ", name='" + name + '\'' +
                ", startTimeNanos=" + startTimeNanos +
                ", duration=" + duration +
                '}';
    }
}
