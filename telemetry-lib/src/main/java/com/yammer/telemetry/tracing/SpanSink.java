package com.yammer.telemetry.tracing;

public interface SpanSink {

    void record(SpanData spanData);

    void recordAnnotation(long traceId, long spanId, AnnotationData annotationData);
}
