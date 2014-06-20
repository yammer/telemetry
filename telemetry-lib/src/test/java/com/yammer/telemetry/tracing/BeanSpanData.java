package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.List;

public class BeanSpanData implements SpanData {
    private int duration;
    private String host;
    private String name;
    private Optional<BigInteger> parentSpanId;
    private BigInteger spanId;
    private long startTime;
    private BigInteger traceId;
    private List<AnnotationData> annotations;

    public BeanSpanData() {
        this.spanId = BigInteger.TEN;
        this.startTime = System.nanoTime();
        traceId = BigInteger.ONE;
        parentSpanId = Optional.absent();
        name = "Some Name";
        host = "Host-001";
        duration = 100;
        annotations = ImmutableList.of();
    }

    public BeanSpanData(int duration, String host, String name, Optional<BigInteger> parentSpanId, BigInteger spanId, long startTime, BigInteger traceId, List<AnnotationData> annotations) {
        this.duration = duration;
        this.host = host;
        this.name = name;
        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
        this.startTime = startTime;
        this.traceId = traceId;
        this.annotations = annotations;
    }

    @Override
    public BigInteger getTraceId() {
        return traceId;
    }

    @Override
    public BigInteger getSpanId() {
        return spanId;
    }

    @Override
    public Optional<BigInteger> getParentSpanId() {
        return parentSpanId;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getHost() {
        return host;
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

    @SuppressWarnings("RedundantIfStatement")
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BeanSpanData that = (BeanSpanData) o;

        if (duration != that.duration) return false;
        if (startTime != that.startTime) return false;
        if (annotations != null ? !annotations.equals(that.annotations) : that.annotations != null) return false;
        if (host != null ? !host.equals(that.host) : that.host != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (parentSpanId != null ? !parentSpanId.equals(that.parentSpanId) : that.parentSpanId != null) return false;
        if (spanId != null ? !spanId.equals(that.spanId) : that.spanId != null) return false;
        if (traceId != null ? !traceId.equals(that.traceId) : that.traceId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = duration;
        result = 31 * result + (host != null ? host.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (parentSpanId != null ? parentSpanId.hashCode() : 0);
        result = 31 * result + (spanId != null ? spanId.hashCode() : 0);
        result = 31 * result + (int) (startTime ^ (startTime >>> 32));
        result = 31 * result + (traceId != null ? traceId.hashCode() : 0);
        result = 31 * result + (annotations != null ? annotations.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "BeanSpanData{" +
                "duration=" + duration +
                ", host='" + host + '\'' +
                ", name='" + name + '\'' +
                ", parentSpanId=" + parentSpanId +
                ", spanId=" + spanId +
                ", startTime=" + startTime +
                ", traceId=" + traceId +
                ", annotations=" + annotations +
                '}';
    }
}
