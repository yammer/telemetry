package com.yammer.telemetry.service.models;

import com.google.common.base.Optional;
import com.yammer.telemetry.tracing.SpanData;

import java.math.BigInteger;

public class BeanSpanData implements SpanData {
    private BigInteger traceId;
    private BigInteger id;
    private Optional<BigInteger> parentId;
    private String name;
    private String host;
    private String serviceName;
    private String serviceHost;
    private long startTimeNanos;
    private long duration;

    private BeanSpanData() { }

    @Override
    public BigInteger getTraceId() {
        return traceId;
    }

    @Override
    public BigInteger getId() {
        return id;
    }

    @Override
    public Optional<BigInteger> getParentId() {
        return parentId;
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
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getServiceHost() {
        return serviceHost;
    }

    @Override
    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    @Override
    public long getDuration() {
        return duration;
    }

    @Override
    public String toString() {
        return "BeanSpanData{" +
                "traceId=" + traceId +
                ", id=" + id +
                ", parentId=" + parentId +
                ", name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceHost='" + serviceHost + '\'' +
                ", startTimeNanos=" + startTimeNanos +
                ", duration=" + duration +
                '}';
    }
}
