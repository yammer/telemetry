package com.yammer.telemetry.tracing;

import java.util.Collection;

public interface SpanSource {

    Collection<Trace> getTraces();

    Trace getTrace(long traceId);
}
