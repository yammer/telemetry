package com.yammer.telemetry.tracing;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import java.util.logging.Logger;

/**
 * Start a new trace.
 * Start a new span within a trace.
 * Attach to a span within a trace.
 */
public class Span implements AutoCloseable, SpanData {
    private static final Logger LOG = Logger.getLogger(Span.class.getName());
    private static final ThreadLocal<SpanContext> spanContext = new ThreadLocal<>();

    private static Sampling sampler = Sampling.ON;
    private static IDGenerator idGenerator = new IDGenerator();

    private final BigInteger traceId;
    private final Optional<BigInteger> parentSpanId;
    private final BigInteger spanId;
    private final String name;
    private final String host;
    private final long startTime;
    private final Integer pid;
    private long duration;
    private final TraceLevel traceLevel;
    private final List<AnnotationData> annotations;

    /**
     * Starts a new trace.
     *
     * @param name Name to be given to the root span in the trace.
     * @return The root span of the newly created trace.
     */
    public static Span startTrace(String name) {
        return start(name, Optional.<BigInteger>absent(), Optional.<BigInteger>absent(), Optional.<BigInteger>absent(), sampler.trace() ? TraceLevel.ON : TraceLevel.OFF);
    }

    /**
     * Starts a new span within a trace. Uses the current thread context to determine the
     * trace ID and parent span ID.
     *
     * @param name Name to be given to the span.
     * @return The newly started span.
     */
    public static Span startSpan(String name) {
        return start(name, Optional.<BigInteger>absent(), Optional.<BigInteger>absent(), Optional.<BigInteger>absent(), TraceLevel.INHERIT);
    }

    /**
     * Attach to an existing span. This is useful when a span has been created elsewhere
     * and you'd like to log annotations against that span locally. For example across thread boundaries in the local
     * vm.
     *
     * @param traceId ID of the trace of the span being attached.
     * @param spanId  ID of the span being attached.
     * @param name    Name for the span - useful for debug
     * @return The attached span.
     */
    public static Span attachSpan(BigInteger traceId, BigInteger spanId, String name) {
        return start(name, Optional.of(traceId), Optional.of(spanId), Optional.<BigInteger>absent(), TraceLevel.ON);
    }

    /**
     * Starts a new span under the specified trace and parent spanId.
     *
     * This is used for attaching to an external span for example with an incoming http request.
     *
     * @param traceId
     * @param parentSpanId
     * @param name
     * @return
     */
    public static Span startSpan(BigInteger traceId, BigInteger parentSpanId, String name) {
        return start(name, Optional.of(traceId), Optional.<BigInteger>absent(), Optional.of(parentSpanId), TraceLevel.ON);
    }

    private static Span start(String name, Optional<BigInteger> traceId, Optional<BigInteger> spanId, Optional<BigInteger> parentSpanId, TraceLevel traceLevel) {
        SpanContext context = spanContext.get();
        if (context == null) {
            context = new SpanContext();
            spanContext.set(context);
        }

        if (!traceId.isPresent()) {
            traceId = context.currentTraceId();
            if (!traceId.isPresent()) {
                traceId = Optional.of(idGenerator.generateTraceId());
            }
        }

        if (!parentSpanId.isPresent()) {
            parentSpanId = context.currentSpanId();
        }

        if (!spanId.isPresent()) {
            spanId = Optional.of(idGenerator.generateSpanId());
        }

        if (traceLevel == TraceLevel.INHERIT) {
            traceLevel = context.currentTraceLevel();
        }

        final Span span = new Span(traceId.get(), spanId.get(), parentSpanId, name, nowInNanoseconds(), System.nanoTime(), traceLevel);
        context.startSpan(span);
        return span;
    }

    private Span(BigInteger traceId, BigInteger spanId, Optional<BigInteger> parentSpanId, String name, long startTime, long startNanos, TraceLevel traceLevel) {
        this.traceId = traceId;
        this.parentSpanId = parentSpanId;
        this.spanId = spanId;
        this.name = name;
        this.host = Annotations.getServiceAnnotations().getHost();
        this.pid = Annotations.getServiceAnnotations().getPid();
        this.startTime = startTime;
        this.duration = startNanos;
        this.traceLevel = traceLevel;
        this.annotations = new LinkedList<>();
    }

    public static Optional<Span> currentSpan() {
        SpanContext context = spanContext.get();
        if (context == null) {
            return Optional.absent();
        } else {
            return context.currentSpan();
        }
    }

    public void addAnnotation(String name) {
        annotations.add(new Annotation(nowInNanoseconds(), name));
    }

    public void addAnnotation(String name, String message) {
        annotations.add(new Annotation(nowInNanoseconds(), name, message));
    }

    public void end() {
        duration = System.nanoTime() - duration;

        // we need to ensure this span context is ended even if it's not being logged,
        // otherwise we risk pollution of the context for subsequent operations.
        SpanContext context = spanContext.get();
        if (context != null) {
            final Iterable<SpanSink> sinks = SpanSinkRegistry.getSpanSinks();
            context.endSpan(this);
            if (traceLevel == TraceLevel.ON) {
                for (SpanSink sink : sinks) {
                    sink.record(this);
                }
            }
        } else {
            throw new IllegalStateException("Span.end() from a detached span.");
        }

        if (traceLevel == TraceLevel.ON) {
            for (SpanSink sink : SpanSinkRegistry.getSpanSinks()) {
                for (AnnotationData annotation : annotations) {
                    sink.recordAnnotation(getTraceId(), getSpanId(), annotation);
                }
            }
        }
    }

    /**
     * This is available for testing to allow checking before and after states are as expected on the span context.
     *
     * @return an immutable list of the current state of spans in the thread local context.
     */
    static ImmutableList<Span> captureSpans() {
        SpanContext context = spanContext.get();
        if (context != null) {
            return ImmutableList.copyOf(context.spans);
        }

        return ImmutableList.of();
    }

    @Override
    public void close() {
        end();
    }

    public BigInteger getTraceId() {
        return traceId;
    }

    public BigInteger getSpanId() {
        return spanId;
    }

    public Optional<BigInteger> getParentSpanId() {
        return parentSpanId;
    }

    public String getName() {
        return name;
    }

    public String getHost() {
        return host;
    }

    @SuppressWarnings("UnusedDeclaration")
    public Integer getPid() {
        return pid;
    }

    public long getStartTime() {
        return startTime;
    }

    public long getDuration() {
        return duration;
    }

    public static Sampling getSampler() {
        return sampler;
    }

    public static void setSampler(Sampling sampler) {
        Span.sampler = sampler;
    }

    private static class SpanContext {
        private final Stack<Span> spans;

        private SpanContext() {
            spans = new Stack<>();
        }

        public Optional<Span> currentSpan() {
            if (spans.isEmpty()) {
                return Optional.absent();
            } else {
                return Optional.of(spans.peek());
            }
        }

        public Optional<BigInteger> currentTraceId() {
            return currentSpan().transform(new Function<Span, BigInteger>() {
                @Override
                public BigInteger apply(Span input) {
                    return input.getTraceId();
                }
            });
        }

        public Optional<BigInteger> currentSpanId() {
            return currentSpan().transform(new Function<Span, BigInteger>() {
                @Override
                public BigInteger apply(Span input) {
                    return input.getSpanId();
                }
            });
        }

        public void startSpan(Span span) {
            spans.push(span);
        }

        public void endSpan(Span span) {
            Span poppedSpan = spans.pop();

            int extraPops = 0;
            while (!poppedSpan.getSpanId().equals(span.getSpanId())) {
                extraPops++;
                poppedSpan = spans.pop();
            }

            if (extraPops > 0) {
                LOG.warning("Popped " + extraPops + " unclosed Spans");
            }
        }

        public TraceLevel currentTraceLevel() {
            if (spans.isEmpty()) {
                if (sampler == Sampling.ON) return TraceLevel.ON;
                return TraceLevel.OFF; // default when not explicitly requested
            } else {
                return spans.peek().traceLevel;
            }
        }
    }

    @Override
    public String toString() {
        return "Span{" +
                "traceId=" + traceId +
                ", parentSpanId=" + parentSpanId +
                ", spanId=" + spanId +
                ", name='" + name + '\'' +
                ", startTime=" + startTime +
                ", duration=" + duration +
                '}';
    }

    private static long nowInNanoseconds() {
        return System.currentTimeMillis() * 1000000;
    }

    private static enum TraceLevel {
        ON, OFF, INHERIT
    }
}
