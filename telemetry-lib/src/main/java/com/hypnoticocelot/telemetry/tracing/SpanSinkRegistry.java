package com.hypnoticocelot.telemetry.tracing;

import com.google.common.collect.ImmutableList;

import java.util.concurrent.atomic.AtomicReference;

public class SpanSinkRegistry {
    private static AtomicReference<ImmutableList<SpanSink>> spanSinks = new AtomicReference<>(ImmutableList.<SpanSink>of());

    public static void register(SpanSink sink) {
        final ImmutableList<SpanSink> oldSinks = spanSinks.get();
        final ImmutableList<SpanSink> newSinks = new ImmutableList.Builder<SpanSink>().addAll(oldSinks).add(sink).build();

        if (!spanSinks.compareAndSet(oldSinks, newSinks)) {
            throw new RuntimeException("Failed to add new SpanSink, concurrent add");
        }
    }

    public static Iterable<SpanSink> getSpanSinks() {
        return spanSinks.get();
    }

    public static void clear() {
        spanSinks.set(ImmutableList.<SpanSink>of());
    }
}
