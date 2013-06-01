package com.hypnoticocelot.telemetry.tracing;

import com.google.common.base.Optional;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Trace {
    private final UUID id;
    private final ConcurrentMap<UUID, List<Span>> childSpans;
    private Span root = null;
    private long startTimeNanos = Long.MAX_VALUE;
    private long duration = 0;

    private Trace(UUID id) {
        this.id = id;
        this.childSpans = new ConcurrentHashMap<>();
    }

    public static Trace startTrace(Span startingSpan) {
        final Trace trace = new Trace(startingSpan.getTraceId());
        trace.addSpan(startingSpan);
        return trace;
    }

    public UUID getId() {
        return id;
    }

    public Span getRoot() {
        return root;
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    public long getDuration() {
        return duration - startTimeNanos;
    }

    public List<Span> getChildren(Span span) {
        return Optional.fromNullable(childSpans.get(span.getId())).or(Collections.<Span>emptyList());
    }

    public void addSpan(Span span) {
        startTimeNanos = Math.min(startTimeNanos, span.getStartTimeNanos());
        duration = Math.max(duration, span.getStartTimeNanos() + span.getDuration());

        final UUID parentId = span.getParentId();
        if (parentId == null) {
            this.root = span;
        } else {
            final List<Span> newSiblings = new LinkedList<>();
            newSiblings.add(span);

            final List<Span> siblings = childSpans.putIfAbsent(parentId, newSiblings);
            if (siblings != null) {
                siblings.add(span);
            }
        }
    }
}
