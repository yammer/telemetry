package com.yammer.telemetry.tracing;

import com.google.common.collect.ImmutableList;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemorySpanSinkSource implements SpanSink, SpanSource {
    public static final int MAX_RECORD_ANNOTATION_ATTEMPTS = 3;
    private final ConcurrentMap<UUID, Trace> traces = new ConcurrentHashMap<>();
    private final ConcurrentMap<UUID, ImmutableList<AnnotationData>> spanAnnotations = new ConcurrentHashMap<>();

    @Override
    public Collection<Trace> getTraces() {
        return traces.values();
    }

    @Override
    public Trace getTrace(UUID traceId) {
        return traces.get(traceId);
    }

    @Override
    public void record(SpanData spanData) {
        // This is cleaner from a code perspective, but it means we allocate a new Trace on
        // every request even if one is already in the map. This may be worth changing if
        // performance suffers here due to the frequency of calls.
        final Trace trace = traces.putIfAbsent(spanData.getTraceId(), Trace.startTrace(spanData));
        if (trace != null) {
            trace.addSpan(spanData);
        }
    }

    @Override
    public void recordAnnotation(UUID spanId, AnnotationData annotation) {
        recordAnnotation(spanId, annotation, 1);
    }

    private void recordAnnotation(UUID spanId, AnnotationData annotation, int attempt) {
        if (attempt > MAX_RECORD_ANNOTATION_ATTEMPTS) {
            throw new RuntimeException("Failed after " + (attempt - 1) + " attempts to record annotation");
        }

        ImmutableList<AnnotationData> annotations = spanAnnotations.get(spanId);
        if (annotations != null || (annotations = spanAnnotations.putIfAbsent(spanId, ImmutableList.of(annotation))) != null) {
            if (!spanAnnotations.replace(spanId, annotations, new ImmutableList.Builder<AnnotationData>().addAll(annotations).add(annotation).build())) {
                recordAnnotation(spanId, annotation, attempt + 1);
            }
        }
    }

    @Override
    public List<AnnotationData> getAnnotations(UUID spanId) {
        return spanAnnotations.get(spanId);
    }
}
