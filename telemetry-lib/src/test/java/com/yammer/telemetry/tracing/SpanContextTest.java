package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.math.BigInteger;
import java.util.EmptyStackException;

import static org.junit.Assert.*;

public class SpanContextTest {
    private final SpanContext context = new SpanContext();

    @Before
    public void setup() {
        SpanHelper.setSampler(Sampling.OFF);
    }

    @After
    public void teardown() {
        SpanHelper.setSampler(Sampling.ON);
    }

    @Test
    public void testCurrentItemsAbsentWhenWhenNoSpanStarted() {
        assertFalse(context.currentSpan().isPresent());
        assertFalse(context.currentTraceId().isPresent());
        assertFalse(context.currentSpanId().isPresent());
        assertEquals(TraceLevel.OFF, context.currentTraceLevel());
    }

    @Test
    public void testCurrentItemsAbsentWhenDisabledSpanStarted() {
        context.startSpan(new DisabledSpan());
        assertTrue(context.currentSpan().isPresent());
        assertFalse(context.currentTraceId().isPresent());
        assertFalse(context.currentSpanId().isPresent());
        assertEquals(TraceLevel.OFF, context.currentTraceLevel());
    }

    @Test
    public void testCurrentItemsPresentWhenEnabledSpanStarted() {
        context.startSpan(new EnabledSpan(BigInteger.ONE, BigInteger.TEN, Optional.<BigInteger>absent(), "span", TraceLevel.INHERIT));
        assertTrue(context.currentSpan().isPresent());
        assertTrue(context.currentTraceId().isPresent());
        assertTrue(context.currentSpanId().isPresent());
        assertEquals(TraceLevel.INHERIT, context.currentTraceLevel());
    }

    @Test(expected = EmptyStackException.class)
    public void testEndingUnknownSpanRaisesEmptyStackException() {
        context.startSpan(new EnabledSpan(BigInteger.ZERO, BigInteger.ONE, Optional.<BigInteger>absent(), "known", TraceLevel.INHERIT));
        context.endSpan(new EnabledSpan(BigInteger.ONE, BigInteger.TEN, Optional.<BigInteger>absent(), "unknown", TraceLevel.INHERIT));
    }

    @Test(expected = EmptyStackException.class)
    public void testEndingSpanWhenNonePresentRaisesEmptyStackException() {
        context.endSpan(new EnabledSpan(BigInteger.ONE, BigInteger.TEN, Optional.<BigInteger>absent(), "unknown", TraceLevel.INHERIT));
    }

    @Test
    public void testEndingKnownSpan() {
        EnabledSpan knownSpan = new EnabledSpan(BigInteger.ZERO, BigInteger.ONE, Optional.<BigInteger>absent(), "known", TraceLevel.INHERIT);
        context.startSpan(knownSpan);
        context.endSpan(knownSpan);

        assertTrue(context.captureSpans().isEmpty());
    }

    @Test
    public void testEndingChildSpanBeforeParentSpanBothEnabled() {
        Span parent = new EnabledSpan(BigInteger.ONE, BigInteger.ZERO, Optional.<BigInteger>absent(), "parent", TraceLevel.ON);
        Span child = new EnabledSpan(BigInteger.ONE, BigInteger.TEN, Optional.of(BigInteger.ZERO), "child", TraceLevel.INHERIT);

        context.startSpan(parent);
        context.startSpan(child);

        context.endSpan(child);
        assertFalse(context.captureSpans().isEmpty());

        context.endSpan(parent);
        assertTrue(context.captureSpans().isEmpty());
    }

    @Test(expected = EmptyStackException.class)
    public void testEndingParentSpanBeforeChildSpanBothEnabled() {
        Span parent = new EnabledSpan(BigInteger.ONE, BigInteger.ZERO, Optional.<BigInteger>absent(), "parent", TraceLevel.ON);
        Span child = new EnabledSpan(BigInteger.ONE, BigInteger.TEN, Optional.of(BigInteger.ZERO), "child", TraceLevel.INHERIT);

        context.startSpan(parent);
        context.startSpan(child);

        context.endSpan(parent);

        assertTrue(context.captureSpans().isEmpty());

        context.endSpan(child);
    }

    @Test
    public void testEndingChildSpanBeforeParentSpanBothDisabled() {
        Span parent = new DisabledSpan();
        Span child = new DisabledSpan();

        context.startSpan(parent);
        context.startSpan(child);

        context.endSpan(child);
        assertFalse(context.captureSpans().isEmpty());

        context.endSpan(parent);
        assertTrue(context.captureSpans().isEmpty());
    }

    @Test(expected = EmptyStackException.class)
    public void testEndingParentSpanBeforeChildSpanBothDisabled() {
        Span parent = new DisabledSpan();
        Span child = new DisabledSpan();

        context.startSpan(parent);
        context.startSpan(child);

        context.endSpan(parent);

        assertTrue(context.captureSpans().isEmpty());

        context.endSpan(child);
    }

    @Test
    public void testEndingChildSpanBeforeParentSpanParentEnabled() {
        Span parent = new EnabledSpan(BigInteger.ONE, BigInteger.ZERO, Optional.<BigInteger>absent(), "parent", TraceLevel.ON);
        Span child = new DisabledSpan();

        context.startSpan(parent);
        context.startSpan(child);

        context.endSpan(child);
        assertFalse(context.captureSpans().isEmpty());

        context.endSpan(parent);
        assertTrue(context.captureSpans().isEmpty());
    }

    @Test(expected = EmptyStackException.class)
    public void testEndingParentSpanBeforeChildSpanParentEnabled() {
        Span parent = new EnabledSpan(BigInteger.ONE, BigInteger.ZERO, Optional.<BigInteger>absent(), "parent", TraceLevel.ON);
        Span child = new DisabledSpan();

        context.startSpan(parent);
        context.startSpan(child);

        context.endSpan(parent);

        assertTrue(context.captureSpans().isEmpty());

        context.endSpan(child);
    }

    @Test
    public void testEndingChildSpanBeforeParentSpanChildEnabled() {
        Span parent = new DisabledSpan();
        Span child = new EnabledSpan(BigInteger.ONE, BigInteger.TEN, Optional.of(BigInteger.ZERO), "child", TraceLevel.INHERIT);

        context.startSpan(parent);
        context.startSpan(child);

        context.endSpan(child);
        assertFalse(context.captureSpans().isEmpty());

        context.endSpan(parent);
        assertTrue(context.captureSpans().isEmpty());
    }

    @Test(expected = EmptyStackException.class)
    public void testEndingParentSpanBeforeChildSpanChildEnabled() {
        Span parent = new DisabledSpan();
        Span child = new EnabledSpan(BigInteger.ONE, BigInteger.TEN, Optional.of(BigInteger.ZERO), "child", TraceLevel.INHERIT);

        context.startSpan(parent);
        context.startSpan(child);

        context.endSpan(parent);

        assertTrue(context.captureSpans().isEmpty());

        context.endSpan(child);
    }
}