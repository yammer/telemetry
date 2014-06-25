package com.yammer.telemetry.tracing;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Stack;
import java.util.logging.Logger;

public class SpanHelper {
    static final ThreadLocal<SpanContext> spanContext = new ThreadLocal<>();
    static final Logger LOG = Logger.getLogger(SpanHelper.class.getName());
    static Sampling sampler = Sampling.ON;
    private static IDGenerator idGenerator = new IDGenerator();

    public static Sampling getSampler() {
        return sampler;
    }

    public static void setSampler(Sampling sampler) {
        SpanHelper.sampler = sampler;
    }

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
     * @param traceId - the current traceId
     * @param parentSpanId - the parent span, if available
     * @param name - the name for this span
     * @return the newly created span
     */
    public static Span startSpan(BigInteger traceId, BigInteger parentSpanId, String name) {
        return start(name, Optional.of(traceId), Optional.<BigInteger>absent(), Optional.of(parentSpanId), TraceLevel.ON);
    }

    public static Optional<Span> currentSpan() {
        SpanContext context = spanContext.get();
        if (context == null) {
            return Optional.absent();
        } else {
            return context.currentSpan();
        }
    }

    private static Span start(String name, Optional<BigInteger> traceId, Optional<BigInteger> spanId, Optional<BigInteger> parentSpanId, TraceLevel traceLevel) {
        SpanContext context = spanContext.get();
        if (context == null) {
            context = new SpanContext();
            spanContext.set(context);
        }

        if (traceLevel == TraceLevel.INHERIT) {
            traceLevel = context.currentTraceLevel();
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

        final Span span = (traceLevel == TraceLevel.OFF) ?
                new DisabledSpan() :
                new EnabledSpan(traceId.get(), spanId.get(), parentSpanId, name, traceLevel);

        context.startSpan(span);
        return span;
    }

    static long nowInNanoseconds() {
        return System.currentTimeMillis() * 1000000;
    }

    static Optional<SpanContext> currentContext() {
        return Optional.fromNullable(spanContext.get());
    }

    static class SpanContext {
        private final Stack<Span> spans;

        SpanContext() {
            spans = new Stack<>();
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

        public Optional<Span> currentSpan() {
            if (spans.isEmpty()) {
                return Optional.absent();
            } else {
                Span span = spans.peek();
                if (span instanceof DisabledSpan) return Optional.absent();
                return Optional.of(span);
            }
        }

        public Optional<BigInteger> currentTraceId() {
            Optional<Span> currentSpan = currentSpan();
            if (!currentSpan.isPresent()) return Optional.absent();
            if (currentSpan.get() instanceof DisabledSpan) return Optional.absent();
            return currentSpan.transform(new Function<Span, BigInteger>() {
                @Override
                public BigInteger apply(Span input) {
                    return input.getTraceId();
                }
            });
        }

        public Optional<BigInteger> currentSpanId() {
            Optional<Span> currentSpan = currentSpan();
            if (!currentSpan.isPresent()) return Optional.absent();
            if (currentSpan.get() instanceof DisabledSpan) return Optional.absent();
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
            if (spans.isEmpty()) {
                LOG.warning("Ending span " + span.getName() + ":" + span.getSpanId() + " when no spans exist in SpanContext");
            }
            Span poppedSpan = spans.pop();

            int extraPops = 0;
            while (!Objects.equals(poppedSpan.getSpanId(), span.getSpanId())) {
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
                return spans.peek().getTraceLevel();
            }
        }
    }
}
