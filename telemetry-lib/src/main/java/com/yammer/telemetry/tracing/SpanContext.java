package com.yammer.telemetry.tracing;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;

import java.math.BigInteger;
import java.util.Objects;
import java.util.Stack;

class SpanContext {
    private final Stack<Span> spans;

    SpanContext() {
        spans = new Stack<>();
    }

    ImmutableList<Span> captureSpans() {
        return ImmutableList.copyOf(spans);
    }

    public Optional<Span> currentSpan() {
        if (spans.isEmpty()) {
            return Optional.absent();
        } else {
            Span span = spans.peek();
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
            SpanHelper.LOG.warning("Ending span " + span.getName() + ":" + span.getSpanId() + " when no spans exist in SpanContext");
        }
        Span poppedSpan = spans.pop();

        int extraPops = 0;
        while (!Objects.equals(poppedSpan, span)) {
            extraPops++;
            poppedSpan = spans.pop();
        }

        if (extraPops > 0) {
            SpanHelper.LOG.warning("Popped " + extraPops + " unclosed Spans");
        }
    }

    public TraceLevel currentTraceLevel() {
        if (spans.isEmpty()) {
            if (SpanHelper.sampler == Sampling.ON) return TraceLevel.ON;
            return TraceLevel.OFF; // default when not explicitly requested
        } else {
            return spans.peek().getTraceLevel();
        }
    }
}
