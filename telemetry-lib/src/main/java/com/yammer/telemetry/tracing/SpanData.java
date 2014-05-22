package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;

import java.math.BigInteger;

public interface SpanData {
    BigInteger getTraceId();

    BigInteger getId();

    Optional<BigInteger> getParentId();

    String getName();

    long getStartTimeNanos();

    long getDuration();
}
