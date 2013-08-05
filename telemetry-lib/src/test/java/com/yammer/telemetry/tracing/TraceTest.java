package com.yammer.telemetry.tracing;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class TraceTest {
    private Span foo;
    private Span bar;
    private Span baz;

    @Before
    public void setUp() {
        foo = Span.startTrace("foo");
        bar = Span.startSpan("bar");
        baz = Span.startSpan("baz");

        baz.end();
        bar.end();
        foo.end();
    }

    @Test
    public void testForward() {
        final Trace trace = Trace.startTrace(foo);
        trace.addSpan(bar);
        trace.addSpan(baz);

        assertEquals(foo, trace.getRoot());
        assertTrue(trace.getChildren(foo).contains(bar));
        assertTrue(trace.getChildren(bar).contains(baz));
        assertTrue(trace.getChildren(baz).isEmpty());
    }

    @Test
    public void testReverse() {
        final Trace trace = Trace.startTrace(baz);
        trace.addSpan(bar);
        trace.addSpan(foo);

        assertEquals(foo, trace.getRoot());
        assertTrue(trace.getChildren(foo).contains(bar));
        assertTrue(trace.getChildren(bar).contains(baz));
        assertTrue(trace.getChildren(baz).isEmpty());
    }

    @Test
    public void testScattered() {
        final Trace trace = Trace.startTrace(bar);
        trace.addSpan(baz);
        trace.addSpan(foo);

        assertEquals(foo, trace.getRoot());
        assertTrue(trace.getChildren(foo).contains(bar));
        assertTrue(trace.getChildren(bar).contains(baz));
        assertTrue(trace.getChildren(baz).isEmpty());
    }

    @Test
    public void testRootless() {
        final Trace trace = Trace.startTrace(baz);
        trace.addSpan(bar);

        assertNull(trace.getRoot());
    }
}
