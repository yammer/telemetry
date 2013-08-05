package com.yammer.telemetry.tracing;

import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Start a new trace.
 * Start a new span within a trace.
 * Attach to a span within a trace.
 */
public class Span implements AutoCloseable, SpanData {
    private static final Logger LOG = Logger.getLogger(Span.class.getName());
    private static final ThreadLocal<SpanContext> spanContext = new ThreadLocal<>();

    private final UUID traceId;
    private final UUID parentId;
    private final UUID id;
    private final String name;
    private final long startTimeNanos;
    private long duration;
    private final boolean logSpan;
    private final List<AnnotationData> annotations;

    /**
     * Starts a new trace.
     *
     * @param name Name to be given to the root span in the trace.
     * @return The root span of the newly created trace.
     */
    public static Span startTrace(String name) {
        return start(name, null, null, null);
    }

    /**
     * Starts a new span within a trace. Uses the current thread context to determine the
     * trace ID and parent span ID.
     *
     * @param name Name to be given to the span.
     * @return The newly started span.
     */
    public static Span startSpan(String name) {
        return start(name, null, null, null);
    }

    /**
     * Attach to an existing span. This is useful when a span has been created elsewhere
     * (probably on another host) and you'd like to log annotations against that span locally.
     *
     * @param traceId ID of the trace of the span being attached.
     * @param spanId ID of the span being attached.
     * @return The attached span.
     */
    public static Span attachSpan(UUID traceId, UUID spanId) {
        return new Span(traceId, spanId, null, null, -1, -1, false);
    }

    private static Span start(String name, UUID traceId, UUID spanId, UUID parentSpanId) {
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

        if (spanId == null) {
            spanId = generateSpanId();
        }
        final Span span = new Span(traceId, spanId, parentSpanId, name, nowInNanoseconds(), System.nanoTime(), true);
        context.startSpan(span);
        return span;
    }

    private Span(UUID traceId, UUID id, UUID parentId, String name, long startTimeNanos, long startNanos, boolean logSpan) {
        this.traceId = traceId;
        this.parentId = parentId;
        this.id = id;
        this.name = name;
        this.startTimeNanos = startTimeNanos;
        this.duration = startNanos;
        this.logSpan = logSpan;
        this.annotations = new LinkedList<>();
    }

    public void addAnnotation(String message) {
        annotations.add(new AnnotationData(nowInNanoseconds(), message));
    }

    public void end() {
        if (logSpan) {
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

        for (SpanSink sink : SpanSinkRegistry.getSpanSinks()) {
            for (AnnotationData annotation : annotations) {
                sink.recordAnnotation(getId(), annotation);
            }
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

    public String getName() {
        return name;
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
    @Override
    public String toString() {
        return "Span{" +
                "traceId=" + traceId +
                ", parentId=" + parentId +
                ", id=" + id +
                ", name='" + name + '\'' +
                ", startTimeNanos=" + startTimeNanos +
                ", duration=" + duration +
                '}';
    }

    private static long nowInNanoseconds() {
        return System.currentTimeMillis() * 1000000;
    }
}
