package com.yammer.telemetry.tracing;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class TraceTest {
    private Span foo;
    private Span bar;
    private Span baz;

    @Rule
    public SpanContextRule spanContextRule = new SpanContextRule();

    @Before
    public void setUp() {
        foo = SpanHelper.startTrace("foo");
        bar = SpanHelper.startSpan("bar");
        baz = SpanHelper.startSpan("baz");

        baz.end();
        bar.end();
        foo.end();
    }

    @Test
    public void testForward() {
        final Trace trace = new Trace(foo.getSpanId());
        trace.addSpan(foo);
        trace.addSpan(bar);
        trace.addSpan(baz);

        assertEquals(foo, trace.getRoot());
        assertTrue(trace.getChildren(foo.getSpanId()).contains(bar));
        assertTrue(trace.getChildren(bar.getSpanId()).contains(baz));
        assertTrue(trace.getChildren(baz.getSpanId()).isEmpty());
    }

    @Test
    public void testReverse() {
        final Trace trace = new Trace(baz.getSpanId());
        trace.addSpan(baz);
        trace.addSpan(bar);
        trace.addSpan(foo);

        assertEquals(foo, trace.getRoot());
        assertTrue(trace.getChildren(foo.getSpanId()).contains(bar));
        assertTrue(trace.getChildren(bar.getSpanId()).contains(baz));
        assertTrue(trace.getChildren(baz.getSpanId()).isEmpty());
    }

    @Test
    public void testScattered() {
        final Trace trace = new Trace(bar.getSpanId());
        trace.addSpan(bar);
        trace.addSpan(baz);
        trace.addSpan(foo);

        assertEquals(foo, trace.getRoot());
        assertTrue(trace.getChildren(foo.getSpanId()).contains(bar));
        assertTrue(trace.getChildren(bar.getSpanId()).contains(baz));
        assertTrue(trace.getChildren(baz.getSpanId()).isEmpty());
    }

    @Test
    public void testRootless() {
        final Trace trace = new Trace(baz.getSpanId());
        trace.addSpan(baz);
        trace.addSpan(bar);

        assertNull(trace.getRoot());
    }
}
