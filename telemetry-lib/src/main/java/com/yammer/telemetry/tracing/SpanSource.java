package com.yammer.telemetry.tracing;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface SpanSource {

    Collection<Trace> getTraces();

    Trace getTrace(UUID traceId);

    List<AnnotationData> getAnnotations(UUID spanId);
}
