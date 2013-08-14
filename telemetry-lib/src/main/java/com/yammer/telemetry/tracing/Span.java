package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;

import java.util.*;
import java.util.logging.Logger;

/**
 * Start a new trace.
 * Start a new span within a trace.
 * Attach to a span within a trace.
 */
public class Span implements AutoCloseable, SpanData {
    private static final Logger LOG = Logger.getLogger(Span.class.getName());
    private static final Random ID_GENERATOR = new Random(System.currentTimeMillis());
    private static final ThreadLocal<SpanContext> spanContext = new ThreadLocal<>();

    private final long traceId;
    private final Optional<Long> parentId;
    private final long id;
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
        return start(name, Optional.<Long>absent(), Optional.<Long>absent(), Optional.<Long>absent(), true);
    }

    /**
     * Starts a new span within a trace. Uses the current thread context to determine the
     * trace ID and parent span ID.
     *
     * @param name Name to be given to the span.
     * @return The newly started span.
     */
    public static Span startSpan(String name) {
        return start(name, Optional.<Long>absent(), Optional.<Long>absent(), Optional.<Long>absent(), true);
    }

    /**
     * Attach to an existing span. This is useful when a span has been created elsewhere
     * (probably on another host) and you'd like to log annotations against that span locally.
     *
     * @param traceId ID of the trace of the span being attached.
     * @param spanId ID of the span being attached.
     * @return The attached span.
     */
    public static Span attachSpan(long traceId, long spanId) {
        return start(null, Optional.of(traceId), Optional.of(spanId), Optional.<Long>absent(), false);
    }

    private static Span start(String name, Optional<Long> traceId, Optional<Long> spanId, Optional<Long> parentSpanId, boolean logSpan) {
        SpanContext context = spanContext.get();
        if (context == null) {
            context = new SpanContext();
            spanContext.set(context);
        }

        if (!traceId.isPresent()) {
            traceId = context.currentTraceId();
            if (!traceId.isPresent()) {
                traceId = Optional.of(generateSpanId());
            }
        }

        if (!parentSpanId.isPresent()) {
            parentSpanId = context.currentSpanId();
        }

        if (!spanId.isPresent()) {
            spanId = Optional.of(generateSpanId());
        }
        final Span span = new Span(traceId.get(), spanId.get(), parentSpanId, name, nowInNanoseconds(), System.nanoTime(), logSpan);
        context.startSpan(span);
        return span;
    }

    private Span(long traceId, long id, Optional<Long> parentId, String name, long startTimeNanos, long startNanos, boolean logSpan) {
        this.traceId = traceId;
        this.parentId = parentId;
        this.id = id;
        this.name = name;
        this.startTimeNanos = startTimeNanos;
        this.duration = startNanos;
        this.logSpan = logSpan;
        this.annotations = new LinkedList<>();
    }

    public void addAnnotation(String name) {
        annotations.add(new Annotation(nowInNanoseconds(), name));
    }

    public void addAnnotation(String name, String message) {
        annotations.add(new Annotation(nowInNanoseconds(), name, message));
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
                sink.recordAnnotation(getTraceId(), getId(), annotation);
            }
        }
    }

    @Override
    public void close() {
        end();
    }

    public long getTraceId() {
        return traceId;
    }

    public long getId() {
        return id;
    }

    public Optional<Long> getParentId() {
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

    private static long generateSpanId() {
        return ID_GENERATOR.nextLong();
    }

    private static class SpanContext {
        private final Stack<Span> spans;

        private SpanContext() {
            spans = new Stack<>();
        }

        public Optional<Long> currentTraceId() {
            if (spans.isEmpty()) {
                return Optional.absent();
            } else {
                return Optional.of(spans.peek().getTraceId());
            }
        }

        public Optional<Long> currentSpanId() {
            if (spans.isEmpty()) {
                return Optional.absent();
            } else {
                return Optional.of(spans.peek().getId());
            }
        }

        public void startSpan(Span span) {
            spans.push(span);
        }

        public void endSpan(Span span) {
            Span poppedSpan = spans.pop();

            int extraPops = 0;
            while (poppedSpan.getId() != span.getId()) {
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
