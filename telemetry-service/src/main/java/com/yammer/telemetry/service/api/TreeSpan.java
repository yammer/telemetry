package com.yammer.telemetry.service.api;

import com.google.common.collect.ImmutableList;
import com.yammer.telemetry.tracing.AnnotationData;
import org.joda.time.DateTime;

import java.util.List;
import java.util.UUID;

public class TreeSpan {
    private final UUID id;
    private final String name;
    private final DateTime startTimeNanos;
    private final long duration;
    private final List<AnnotationData> annotations;
    private final ImmutableList<TreeSpan> children;

    public TreeSpan(UUID id, String name, DateTime startTimeNanos, long duration, List<AnnotationData> annotations, ImmutableList<TreeSpan> children) {
        this.id = id;
        this.name = name;
        this.startTimeNanos = startTimeNanos;
        this.duration = duration;
        this.annotations = annotations;
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

    public List<AnnotationData> getAnnotations() {
        return annotations;
    }

    public ImmutableList<TreeSpan> getChildren() {
        return children;
    }
}
