package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;

/**
 * Start a new trace.
 * Start a new span within a trace.
 * Attach to a span within a trace.
 */
public class EnabledSpan extends Span {

    private final long startTime;
    private long duration;
    private final TraceLevel traceLevel;
    private final List<AnnotationData> annotations;

    EnabledSpan(BigInteger traceId, BigInteger spanId, Optional<BigInteger> parentSpanId, String name, long startTime, long startNanos, TraceLevel traceLevel) {
        super(parentSpanId, spanId, name, traceId);
        this.startTime = startTime;
        this.duration = startNanos;
        this.traceLevel = traceLevel;
        this.annotations = new LinkedList<>();
    }

    @Override
    public void addAnnotation(String name) {
        annotations.add(new Annotation(SpanHelper.nowInNanoseconds(), name));
    }

    @Override
    public void addAnnotation(String name, String message) {
        annotations.add(new Annotation(SpanHelper.nowInNanoseconds(), name, message));
    }

    @Override
    public void end() {
        duration = System.nanoTime() - duration;

        // we need to ensure this span context is ended even if it's not being logged,
        // otherwise we risk pollution of the context for subsequent operations.
        SpanHelper.SpanContext context = SpanHelper.spanContext.get();
        if (context != null) {
            final Iterable<SpanSink> sinks = SpanSinkRegistry.getSpanSinks();
            context.endSpan(this);
            if (getTraceLevel() == TraceLevel.ON) {
                for (SpanSink sink : sinks) {
                    sink.record(this);
                }
            }
        } else {
            throw new IllegalStateException("Span.end() from a detached span.");
        }
    }

    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public List<AnnotationData> getAnnotations() {
        return annotations;
    }

    @Override
    TraceLevel getTraceLevel() {
        return traceLevel;
    }

    @Override
    public String toString() {
        return "Span{" +
                "traceId=" + traceId +
                ", parentSpanId=" + parentSpanId +
                ", spanId=" + spanId +
                ", name='" + name + '\'' +
                ", startTime=" + startTime +
                ", duration=" + duration +
                '}';
    }

}
