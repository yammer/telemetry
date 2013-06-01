package com.hypnoticocelot.telemetry.example.sink;

import com.google.common.collect.ImmutableMap;
import com.hypnoticocelot.telemetry.tracing.Span;
import com.hypnoticocelot.telemetry.tracing.SpanSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class SLF4JSpanSink implements SpanSink {
    private static final Logger LOG = LoggerFactory.getLogger(SLF4JSpanSink.class);

    @Override
    public void record(Span span) {
        LOG.trace("[TELEMETRY id={}; pid={}; tid={}; name={}; start={}; end={}; annotations={}]",
                span.getId(),
                span.getParentId(),
                span.getTraceId(),
                span.getData().getName(),
                span.getStartTime(),
                span.getEndTime(),
                renderAnnotations(span.getData().getAnnotations()));
    }

    private String renderAnnotations(ImmutableMap<String, String> annotations) {
        final StringBuilder builder = new StringBuilder();
        builder.append("{");
        for (Map.Entry<String, String> entry : annotations.entrySet()) {
            builder.append(entry.getKey()).append("=").append(entry.getValue()).append(" ");
        }
        builder.append("}");
        return builder.toString();
    }
}
