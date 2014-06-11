package com.yammer.telemetry.tracing;

import java.math.BigInteger;
import java.util.Collection;

public interface SpanSource {

    Collection<Trace> getTraces();

    Trace getTrace(BigInteger traceId);
}
