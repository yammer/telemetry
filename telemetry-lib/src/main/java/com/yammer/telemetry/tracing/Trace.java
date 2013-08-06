package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Trace {
    private final UUID id;
    private final ConcurrentMap<UUID, List<SpanData>> childSpans;
    private final ConcurrentMap<UUID, List<AnnotationData>> annotations;
    private SpanData root = null;
    private long startTimeNanos = Long.MAX_VALUE;
    private long duration = 0;

    public Trace(UUID id) {
        this.id = id;
        this.childSpans = new ConcurrentHashMap<>();
        this.annotations = new ConcurrentHashMap<>();
    }

    public UUID getId() {
        return id;
    }

    public SpanData getRoot() {
        return root;
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    public long getDuration() {
        return duration - startTimeNanos;
    }

    public List<SpanData> getChildren(SpanData spanData) {
        return Optional.fromNullable(childSpans.get(spanData.getId())).or(Collections.<SpanData>emptyList());
    }

    public List<AnnotationData> getAnnotations(SpanData spanData) {
        return annotations.get(spanData.getId());
    }

    public void addSpan(SpanData spanData) {
        startTimeNanos = Math.min(startTimeNanos, spanData.getStartTimeNanos());
        duration = Math.max(duration, spanData.getStartTimeNanos() + spanData.getDuration());

        final UUID parentId = spanData.getParentId();
        if (parentId == null) {
            this.root = spanData;
        } else {
            final List<SpanData> newSiblings = new LinkedList<>();
            newSiblings.add(spanData);

            final List<SpanData> siblings = childSpans.putIfAbsent(parentId, newSiblings);
            if (siblings != null) {
                siblings.add(spanData);
            }
        }
    }

    public void addAnnotation(UUID spanId, AnnotationData data) {
        final LinkedList<AnnotationData> currentAnnotation = new LinkedList<>();
        currentAnnotation.add(data);
        List<AnnotationData> previousAnnotations = annotations.putIfAbsent(spanId, currentAnnotation);
        if (previousAnnotations != null) {
            previousAnnotations.add(data);
        }
    }
}
