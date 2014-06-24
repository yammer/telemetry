package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;

import java.math.BigInteger;
import java.util.List;

public abstract class Span implements AutoCloseable, SpanData {
    protected final BigInteger traceId;
    protected final Optional<BigInteger> parentSpanId;
    protected final BigInteger spanId;
    protected final String name;
    protected final String host;
    protected final Integer pid;

    protected Span(Optional<BigInteger> parentSpanId, BigInteger spanId, String name, BigInteger traceId) {
        this.host = Annotations.getServiceAnnotations().getHost();
        this.pid = Annotations.getServiceAnnotations().getPid();
        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
        this.name = name;
        this.traceId = traceId;
    }

    public abstract void addAnnotation(String name);

    public abstract void addAnnotation(String name, String message);

    public abstract void end();

    @Override
    public void close() {
        end();
    }

    public BigInteger getTraceId() {
        return traceId;
    }

    public BigInteger getSpanId() {
        return spanId;
    }

    public Optional<BigInteger> getParentSpanId() {
        return parentSpanId;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Integer getPid() {
        return pid;
    }

    public abstract long getStartTime();

    public abstract long getDuration();

    @Override
    public abstract List<AnnotationData> getAnnotations();

    abstract TraceLevel getTraceLevel();
}
