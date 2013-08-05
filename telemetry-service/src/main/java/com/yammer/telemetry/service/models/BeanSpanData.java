package com.yammer.telemetry.service.models;

import com.yammer.telemetry.tracing.SpanData;

import java.util.UUID;

public class BeanSpanData implements SpanData {
    private UUID traceId;
    private UUID id;
    private UUID parentId;
    private String name;
    private long startTimeNanos;
    private long duration;

    private BeanSpanData() { }

    @Override
    public UUID getTraceId() {
        return traceId;
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public UUID getParentId() {
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
