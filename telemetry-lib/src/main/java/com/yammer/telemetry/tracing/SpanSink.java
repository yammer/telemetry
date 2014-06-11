package com.yammer.telemetry.tracing;

import java.math.BigInteger;

public interface SpanSink {

    void record(SpanData spanData);

    void recordAnnotation(BigInteger traceId, BigInteger spanId, AnnotationData annotationData);
}
