package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Trace {
    private final BigInteger id;
    private final ConcurrentMap<BigInteger, List<SpanData>> childSpans;
    private final ConcurrentMap<BigInteger, List<AnnotationData>> annotations;
    private SpanData root = null;
    private long startTimeNanos = Long.MAX_VALUE;
    private long duration = 0;

    public Trace(BigInteger id) {
        this.id = id;
        this.childSpans = new ConcurrentHashMap<>();
        this.annotations = new ConcurrentHashMap<>();
    }

    public BigInteger getId() {
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
        if (spanData == null) {
            return Collections.emptyList();
        }
        return Optional.fromNullable(childSpans.get(spanData.getId())).or(Collections.<SpanData>emptyList());
    }

    public List<AnnotationData> getAnnotations(SpanData spanData) {
        if (spanData == null) {
            return Collections.emptyList();
        }
        return Optional.fromNullable(annotations.get(spanData.getId())).or(Collections.<AnnotationData>emptyList());
    }

    public void addSpan(SpanData spanData) {
        startTimeNanos = Math.min(startTimeNanos, spanData.getStartTimeNanos());
        duration = Math.max(duration, spanData.getStartTimeNanos() + spanData.getDuration());

        final Optional<BigInteger> parentId = spanData.getParentId();
        if (!parentId.isPresent()) {
            this.root = spanData;
        } else {
            final List<SpanData> newSiblings = new LinkedList<>();
            newSiblings.add(spanData);

            final List<SpanData> siblings = childSpans.putIfAbsent(parentId.get(), newSiblings);
            if (siblings != null) {
                siblings.add(spanData);
            }
        }
    }

    public void addAnnotation(BigInteger spanId, AnnotationData data) {
        final LinkedList<AnnotationData> currentAnnotation = new LinkedList<>();
        currentAnnotation.add(data);
        List<AnnotationData> previousAnnotations = annotations.putIfAbsent(spanId, currentAnnotation);
        if (previousAnnotations != null) {
            previousAnnotations.add(data);
        }
    }
}
