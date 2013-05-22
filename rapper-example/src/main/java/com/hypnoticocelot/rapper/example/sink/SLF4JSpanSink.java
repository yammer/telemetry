package com.hypnoticocelot.rapper.example.sink;

import com.hypnoticocelot.rapper.tracing.Span;
import com.hypnoticocelot.rapper.tracing.SpanSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SLF4JSpanSink implements SpanSink {
    private static final Logger LOG = LoggerFactory.getLogger(SLF4JSpanSink.class);

    @Override
    public void record(Span span) {
        LOG.trace("[RAPPER id={}; pid={}; tid={}; name={}; start={}; end={}]",
                span.getId(),
                span.getParentId(),
                span.getTraceId(),
                span.getName(),
                span.getStartTime(),
                span.getEndTime());
    }
}
