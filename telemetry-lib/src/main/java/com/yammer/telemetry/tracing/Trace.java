package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;

import java.math.BigInteger;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Trace {
    private final BigInteger traceId;
    private final ConcurrentMap<BigInteger, List<SpanData>> childSpans;
    private final ConcurrentMap<BigInteger, List<AnnotationData>> annotations;
    private SpanData root = null;

    public Trace(BigInteger traceId) {
        this.traceId = traceId;
        this.childSpans = new ConcurrentHashMap<>();
        this.annotations = new ConcurrentHashMap<>();
    }

    public BigInteger getTraceId() {
        return traceId;
    }

    public SpanData getRoot() {
        return root;
    }

    public List<SpanData> getChildren(BigInteger spanId) {
        if (spanId == null) {
            return Collections.emptyList();
        }
        return Optional.fromNullable(childSpans.get(spanId)).or(Collections.<SpanData>emptyList());
    }

    public List<AnnotationData> getAnnotations(BigInteger spanId) {
        if (spanId == null) {
            return Collections.emptyList();
        }
        return Optional.fromNullable(annotations.get(spanId)).or(Collections.<AnnotationData>emptyList());
    }

    public void addSpan(SpanData spanData) {
        final Optional<BigInteger> parentSpanId = spanData.getParentSpanId();
        if (!parentSpanId.isPresent()) {
            this.root = spanData;
        } else {
            final List<SpanData> newSiblings = new LinkedList<>();
            newSiblings.add(spanData);

            final List<SpanData> siblings = childSpans.putIfAbsent(parentSpanId.get(), newSiblings);
            if (siblings != null) {
                siblings.add(spanData);
            }
        }

        for (AnnotationData annotation : spanData.getAnnotations()) {
            addAnnotation(spanData.getSpanId(), annotation);
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
