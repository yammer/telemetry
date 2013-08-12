package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;
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

        final Span span = Span.startTrace("testSpan");

        span.end();
        verify(sink).record(span);

        assertNotNull(span.getId());
        assertEquals(Optional.absent(), span.getParentId());
        assertEquals("testSpan", span.getName());
        assertTrue(span.getDuration() >= 0);
    }

    @Test
    public void testRootlessSpans() {
        final Span outer = Span.startTrace("outerSpan");
        final Span inner = Span.startSpan("innerSpan");
        inner.end();
        outer.end();
    }

    @Test
    public void testNestedSpan() {
        final SpanSink sink = mock(SpanSink.class);
        SpanSinkRegistry.register(sink);

        final Span outer = Span.startTrace("outerSpan");
        final Span inner = Span.startSpan("innerSpan");

        inner.end();
        verify(sink).record(inner);

        outer.end();
        verify(sink).record(outer);

        assertEquals(Optional.of(outer.getId()), inner.getParentId());
    }

    @Test
    public void testMultipleSinks() {
        final SpanSink first = mock(SpanSink.class);
        final SpanSink second = mock(SpanSink.class);
        SpanSinkRegistry.register(first);
        SpanSinkRegistry.register(second);

        final Span span = Span.startTrace("testSpan");
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
        assertEquals(Optional.absent(), spans.get(0).getParentId());
        assertEquals(Optional.absent(), spans.get(1).getParentId());
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
                final Span span = Span.startTrace(spanName);
                barrier.await();
                span.end();
            } catch (Exception e) {
                throw new RuntimeException("Problem tracing a span", e);
            }
        }
    }
}
