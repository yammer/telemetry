package com.hypnoticocelot.telemetry.service.models;

import com.hypnoticocelot.telemetry.tracing.SpanData;
import com.hypnoticocelot.telemetry.tracing.SpanInfo;

import java.util.UUID;

public class BeanSpanData implements SpanData {
    private UUID traceId;
    private UUID id;
    private UUID parentId;
    private SpanInfo info;
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
    public SpanInfo getInfo() {
        return info;
    }

    @Override
    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    @Override
    public long getDuration() {
        return duration;
    }
}
