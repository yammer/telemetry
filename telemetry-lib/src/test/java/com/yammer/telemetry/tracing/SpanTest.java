package com.yammer.telemetry.tracing;

import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.concurrent.CyclicBarrier;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class SpanTest {
    @After
    public void clearSpanSinkRegistry() {
        SpanSinkRegistry.clear();
    }

    @Test
    public void testRootSpan() {
        final SpanSink sink = mock(SpanSink.class);
        SpanSinkRegistry.register(sink);

        final Span span = Span.start("testSpan");

        span.end();
        verify(sink).record(span);

        assertNotNull(span.getId());
        assertNull(span.getParentId());
        assertEquals("testSpan", span.getName());
        assertTrue(span.getDuration() >= 0);
    }

    @Test
    public void testRootlessSpans() {
        final Span outer = Span.start("outerSpan");
        final Span inner = Span.start("innerSpan");
        inner.end();
        outer.end();
    }

    @Test
    public void testNestedSpan() {
        final SpanSink sink = mock(SpanSink.class);
        SpanSinkRegistry.register(sink);

        final Span outer = Span.start("outerSpan");
        final Span inner = Span.start("innerSpan");

        inner.end();
        verify(sink).record(inner);

        outer.end();
        verify(sink).record(outer);

        assertEquals(outer.getId(), inner.getParentId());
    }

    @Test
    public void testMultipleSinks() {
        final SpanSink first = mock(SpanSink.class);
        final SpanSink second = mock(SpanSink.class);
        SpanSinkRegistry.register(first);
        SpanSinkRegistry.register(second);

        final Span span = Span.start("testSpan");
        span.end();

        verify(first).record(span);
        verify(second).record(span);
    }

    @Test
    public void testMultipleThreads() throws InterruptedException {
        final SpanSink sink = mock(SpanSink.class);
        SpanSinkRegistry.register(sink);
        final CyclicBarrier inside = new CyclicBarrier(2);

        final Thread one = new Thread(new BarrierSpanRunner("threadOne", inside));
        final Thread two = new Thread(new BarrierSpanRunner("threadTwo", inside));

        one.start();
        two.start();

        one.join();
        two.join();

        final ArgumentCaptor<Span> captor = ArgumentCaptor.forClass(Span.class);
        verify(sink, times(2)).record(captor.capture());

        final List<Span> spans = captor.getAllValues();
        assertEquals(2, spans.size());
        assertNull(spans.get(0).getParentId());
        assertNull(spans.get(1).getParentId());
    }

    private class BarrierSpanRunner implements Runnable {
        private final String spanName;
        private final CyclicBarrier barrier;

        public BarrierSpanRunner(String spanName, CyclicBarrier barrier) {
            this.spanName = spanName;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            try {
                final Span span = Span.start(spanName);
                barrier.await();
                span.end();
            } catch (Exception e) {
                throw new RuntimeException("Problem tracing a span", e);
            }
        }
    }
}
