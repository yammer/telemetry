package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;

public class DisabledSpan extends Span {
    public DisabledSpan(Optional<BigInteger> parentSpanId, BigInteger spanId, String name, BigInteger traceId) {
        super(parentSpanId, spanId, name, traceId);
    }

    @Override
    public void addAnnotation(String name) {
        // noop
    }

    @Override
    public void addAnnotation(String name, String message) {
        // noop
    }

    @Override
    public void end() {
        // todo - remove from SpanContext
    }

    @Override
    public long getStartTime() {
        return 0;
    }

    @Override
    public long getDuration() {
        return 0;
    }

    @Override
    public List<AnnotationData> getAnnotations() {
        return ImmutableList.of();
    }

    @Override
    TraceLevel getTraceLevel() {
        return TraceLevel.OFF;
    }
}
