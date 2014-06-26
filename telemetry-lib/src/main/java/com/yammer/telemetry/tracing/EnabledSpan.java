package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;

import java.math.BigInteger;

public class EnabledSpan extends Span {
    EnabledSpan(BigInteger traceId, BigInteger spanId, Optional<BigInteger> parentSpanId, String name, TraceLevel traceLevel) {
        super(parentSpanId, spanId, name, traceId, SpanHelper.nowInNanoseconds(), System.nanoTime(), traceLevel);
    }

    @Override
    public void addAnnotation(String name) {
        annotations.add(new Annotation(SpanHelper.nowInNanoseconds(), name));
    }

    @Override
    public void addAnnotation(String name, String message) {
        annotations.add(new Annotation(SpanHelper.nowInNanoseconds(), name, message));
    }

    protected void afterClose() {
        final Iterable<SpanSink> sinks = SpanSinkRegistry.getSpanSinks();
        if (getTraceLevel() == TraceLevel.ON) {
            for (SpanSink sink : sinks) {
                sink.record(this);
            }
        }
    }
}
