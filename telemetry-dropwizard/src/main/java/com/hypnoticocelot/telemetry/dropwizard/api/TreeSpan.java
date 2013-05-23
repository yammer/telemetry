package com.hypnoticocelot.telemetry.dropwizard.api;

import org.joda.time.DateTime;

import java.util.UUID;

public class TreeSpan {
    private final UUID id;
    private final String name;
    private final DateTime startTime;
    private final DateTime endTime;
    private final TreeSpan[] children;

    public TreeSpan(UUID id, String name, DateTime startTime, DateTime endTime, TreeSpan[] children) {
        this.id = id;
        this.name = name;
        this.startTime = startTime;
        this.endTime = endTime;
        this.children = children;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public DateTime getStartTime() {
        return startTime;
    }

    public DateTime getEndTime() {
        return endTime;
    }

    public TreeSpan[] getChildren() {
        return children;
    }
}
