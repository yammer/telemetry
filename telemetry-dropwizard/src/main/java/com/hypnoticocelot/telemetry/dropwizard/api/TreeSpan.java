package com.hypnoticocelot.telemetry.dropwizard.api;

import org.joda.time.DateTime;

import java.util.UUID;

public class TreeSpan {
    private final UUID id;
    private final String name;
    private final DateTime startTimeNanos;
    private final long duration;
    private final TreeSpan[] children;

    public TreeSpan(UUID id, String name, DateTime startTimeNanos, long duration, TreeSpan[] children) {
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

    public TreeSpan[] getChildren() {
        return children;
    }
}
