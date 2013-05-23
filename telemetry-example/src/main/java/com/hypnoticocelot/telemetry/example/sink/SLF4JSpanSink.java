package com.hypnoticocelot.telemetry.example.sink;

import com.hypnoticocelot.telemetry.tracing.Span;
import com.hypnoticocelot.telemetry.tracing.SpanSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4JSpanSink implements SpanSink {
    private static final Logger LOG = LoggerFactory.getLogger(SLF4JSpanSink.class);

    @Override
    public void record(Span span) {
        LOG.trace("[TELEMETRY id={}; pid={}; tid={}; name={}; start={}; end={}]",
                span.getId(),
                span.getParentId(),
                span.getTraceId(),
                span.getName(),
                span.getStartTime(),
                span.getEndTime());
    }
}
