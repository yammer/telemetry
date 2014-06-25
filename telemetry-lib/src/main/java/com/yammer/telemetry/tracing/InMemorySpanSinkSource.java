package com.yammer.telemetry.tracing;

import java.math.BigInteger;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class InMemorySpanSinkSource implements SpanSink {
    private final ConcurrentMap<BigInteger, Trace> traces = new ConcurrentHashMap<>();

    public Collection<Trace> getTraces() {
        return traces.values();
    }

    public Trace getTrace(BigInteger traceId) {
        return traceId == null ? null : traces.get(traceId);
    }

    @Override
    public void record(SpanData spanData) {
        // This is cleaner from a code perspective, but it means we allocate a new Trace on
        // every request even if one is already in the map. This may be worth changing if
        // performance suffers here due to the frequency of calls.
        if (spanData instanceof DisabledSpan) throw new IllegalArgumentException("Should never be recording disabled spans");

        final Trace newTrace = new Trace(spanData.getTraceId());
        newTrace.addSpan(spanData);

        final Trace trace = traces.putIfAbsent(spanData.getTraceId(), newTrace);
        if (trace != null) {
            trace.addSpan(spanData);
        }
    }

    public int recordedTraceCount() {
        return traces.size();
    }
}
