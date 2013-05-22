package com.hypnoticocelot.rapper.tracing;

import java.util.Collection;
import java.util.UUID;

public interface SpanSource {

    Collection<Trace> getTraces();

    Trace getTrace(UUID traceId);
}
