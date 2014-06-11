package com.yammer.telemetry.service.models;

import com.google.common.base.Optional;
import com.yammer.telemetry.tracing.SpanData;

import java.math.BigInteger;

public class BeanSpanData implements SpanData {
    private BigInteger traceId;
    private BigInteger id;
    private Optional<BigInteger> parentSpanId;
    private String name;
    private String host;
    private String serviceName;
    private String serviceHost;
    private long startTime;
    private long duration;

    private BeanSpanData() { }

    @Override
    public BigInteger getTraceId() {
        return traceId;
    }

    @Override
    public BigInteger getSpanId() {
        return id;
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
    public String getServiceName() {
        return serviceName;
    }

    @Override
    public String getServiceHost() {
        return serviceHost;
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
    public String toString() {
        return "BeanSpanData{" +
                "traceId=" + traceId +
                ", id=" + id +
                ", parentSpanId=" + parentSpanId +
                ", name='" + name + '\'' +
                ", host='" + host + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceHost='" + serviceHost + '\'' +
                ", startTime=" + startTime +
                ", duration=" + duration +
                '}';
    }
}
