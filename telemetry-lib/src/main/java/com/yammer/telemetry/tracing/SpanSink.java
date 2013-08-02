package com.yammer.telemetry.tracing;

import java.util.UUID;

public interface SpanSink {

    void record(SpanData spanData);

    void recordAnnotation(UUID spanId, AnnotationData annotation);
}
