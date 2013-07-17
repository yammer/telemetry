package com.hypnoticocelot.telemetry.service.api;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;

import java.util.UUID;

public class TreeSpan {
    private final UUID id;
    private final String name;
    private final DateTime startTimeNanos;
    private final long duration;
    private final ImmutableList<TreeSpan> children;

    public TreeSpan(UUID id, String name, DateTime startTimeNanos, long duration, ImmutableList<TreeSpan> children) {
        this.id = id;
        this.name = name;
        this.startTimeNanos = startTimeNanos;
        this.duration = duration;
        this.children = children;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DateTime getStartTimeNanos() {
        return startTimeNanos;
    }

    public long getDuration() {
        return duration;
    }

    public ImmutableList<TreeSpan> getChildren() {
        return children;
    }
}
