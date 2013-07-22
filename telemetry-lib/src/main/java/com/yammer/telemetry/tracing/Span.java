package com.yammer.telemetry.tracing;

import java.util.Stack;
import java.util.UUID;
import java.util.logging.Logger;

public class Span implements AutoCloseable, SpanData {
    private static final Logger LOG = Logger.getLogger(Span.class.getName());
    private static final ThreadLocal<SpanContext> spanContext = new ThreadLocal<>();

    private final UUID traceId;
    private final UUID id;
    private final UUID parentId;
    private final SpanInfo info;
    private final long startTimeNanos;
    private long duration;

    public static Span start(SpanInfo info) {
        return start(info, null, null);
    }

    public static Span start(SpanInfo info, UUID traceId, UUID parentSpanId) {
        SpanContext context = spanContext.get();
        if (context == null) {
            context = new SpanContext();
            spanContext.set(context);
        }

        if (traceId == null) {
            traceId = context.currentTraceId();
            if (traceId == null) {
                traceId = UUID.randomUUID();
            }
        }

        if (parentSpanId == null) {
            parentSpanId = context.currentSpanId();
        }

        final UUID spanId = generateSpanId();
        final Span span = new Span(traceId, spanId, parentSpanId, info, System.currentTimeMillis() * 1000000, System.nanoTime());
        context.startSpan(span);
        return span;
    }

    private Span(UUID traceId, UUID id, UUID parentId, SpanInfo info, long startTimeNanos, long startNanos) {
        this.traceId = traceId;
        this.parentId = parentId;
        this.id = id;
        this.info = info;
        this.startTimeNanos = startTimeNanos;
        this.duration = startNanos;
    }

    public void end() {
        duration = System.nanoTime() - duration;

        SpanContext context = spanContext.get();
        if (context != null) {
            final Iterable<SpanSink> sinks = SpanSinkRegistry.getSpanSinks();
            context.endSpan(this);
            for (SpanSink sink : sinks) {
                sink.record(this);
            }
        } else {
            throw new IllegalStateException("Span.end() from a detached span.");
        }
    }

    @Override
    public void close() {
        end();
    }

    public UUID getTraceId() {
        return traceId;
    }

    public UUID getId() {
        return id;
    }

    public UUID getParentId() {
        return parentId;
    }

    public SpanInfo getInfo() {
        return info;
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    public long getDuration() {
        return duration;
    }

    public static UUID currentTraceId() {
        return spanContext.get().currentTraceId();
    }

    public static UUID currentSpanId() {
        return spanContext.get().currentSpanId();
    }

    private static UUID generateSpanId() {
        return UUID.randomUUID();
    }

    private static class SpanContext {
        private final Stack<Span> spans;

        private SpanContext() {
            spans = new Stack<>();
        }

        public UUID currentTraceId() {
            if (spans.isEmpty()) {
                return null;
            } else {
                return spans.peek().getTraceId();
            }
        }

        public UUID currentSpanId() {
            if (spans.isEmpty()) {
                return null;
            } else {
                return spans.peek().getId();
            }
        }

        public void startSpan(Span span) {
            spans.push(span);
        }

        public void endSpan(Span span) {
            Span poppedSpan = spans.pop();

            int extraPops = 0;
            while (!poppedSpan.getId().equals(span.getId())) {
                extraPops++;
                poppedSpan = spans.pop();
            }

            if (extraPops > 0) {
                LOG.warning("Popped " + extraPops + " unclosed Spans");
            }
        }
    }
}
