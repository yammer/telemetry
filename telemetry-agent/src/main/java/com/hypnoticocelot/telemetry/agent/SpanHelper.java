package com.hypnoticocelot.telemetry.agent;

import com.hypnoticocelot.telemetry.tracing.Span;
import com.hypnoticocelot.telemetry.tracing.SpanInfo;

import java.util.Stack;

public class SpanHelper {
    private static final ThreadLocal<Stack<Span>> currentSpan = new ThreadLocal<Stack<Span>>() {
        @Override
        protected Stack<Span> initialValue() {
            return new Stack<>();
        }
    };

    public static void startSpan(SpanInfo info) {
        currentSpan.get().push(Span.start(info));
    }

    public static void endSpan() {
        if (!currentSpan.get().empty()) {
            currentSpan.get().pop().end();
        }
    }
}
