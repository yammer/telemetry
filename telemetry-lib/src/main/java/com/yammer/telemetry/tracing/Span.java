package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

/**
 * Start a new trace.
 * Start a new span within a trace.
 * Attach to a span within a trace.
 */
public abstract class Span implements AutoCloseable, SpanData {
    private final BigInteger traceId;
    private final Optional<BigInteger> parentSpanId;
    private final BigInteger spanId;
    private final String name;
    private final String host;
    private final Integer pid;
    private final TraceLevel traceLevel;
    private final long startTime;
    protected final List<AnnotationData> annotations;
    private final UUID guid;
    private long duration;

    protected Span(Optional<BigInteger> parentSpanId, BigInteger spanId, String name, BigInteger traceId, long startTime, long startNanos, TraceLevel traceLevel) {
        this.guid = UUID.randomUUID();
        this.host = Annotations.getServiceAnnotations().getHost();
        this.pid = Annotations.getServiceAnnotations().getPid();
        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
        this.name = name;
        this.traceId = traceId;
        this.startTime = startTime;
        this.duration = startNanos;
        this.traceLevel = traceLevel;
        this.annotations = new LinkedList<>();
    }

    public abstract void addAnnotation(String name);

    public abstract void addAnnotation(String name, String message);

    protected abstract void afterClose();

    public final void end() {
        duration = System.nanoTime() - duration;

        // we need to ensure this span context is ended even if it's not being logged,
        // otherwise we risk pollution of the context for subsequent operations.
        Optional<SpanContext> context = SpanHelper.currentContext();
        if (context.isPresent()) {
            context.get().endSpan(this);
            afterClose();
        } else {
            throw new IllegalStateException("Span.end() from a detached span.");
        }
    }

    @Override
    public final void close() {
        end();
    }

    public final BigInteger getTraceId() {
        return traceId;
    }

    public final BigInteger getSpanId() {
        return spanId;
    }

    public final Optional<BigInteger> getParentSpanId() {
        return parentSpanId;
    }

    public final String getName() {
        return name;
    }

    public final String getHost() {
        return host;
    }

    @SuppressWarnings("UnusedDeclaration")
    public final Integer getPid() {
        return pid;
    }

    @Override
    public final long getStartTime() {
        return startTime;
    }

    @Override
    public final long getDuration() {
        return duration;
    }

    @Override
    public final List<AnnotationData> getAnnotations() {
        return annotations;
    }

    final TraceLevel getTraceLevel() {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Span span = (Span) o;

        return guid.equals(span.guid);
    }

    @Override
    public int hashCode() {
        return guid.hashCode();
    }
}
