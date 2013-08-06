package com.yammer.telemetry.tracing;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemorySpanSinkSource implements SpanSink, SpanSource {
    private final ConcurrentMap<UUID, Trace> traces = new ConcurrentHashMap<>();

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
        final Trace newTrace = new Trace(spanData.getTraceId());
        newTrace.addSpan(spanData);

        final Trace trace = traces.putIfAbsent(spanData.getTraceId(), newTrace);
        if (trace != null) {
            trace.addSpan(spanData);
        }
    }

    @Override
    public void recordAnnotation(UUID traceId, UUID spanId, AnnotationData annotation) {
        final Trace newTrace = new Trace(traceId);
        newTrace.addAnnotation(spanId, annotation);

        final Trace trace = traces.putIfAbsent(traceId, newTrace);
        if (trace != null) {
            trace.addAnnotation(spanId, annotation);
        }
    }
}
