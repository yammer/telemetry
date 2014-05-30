package com.yammer.telemetry.tracing;

import com.google.common.collect.ImmutableList;
import org.junit.After;
import org.junit.Test;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SpanSinkRegistryTest {
    @After
    public void resetRegistry() {
        SpanSinkRegistry.clear();
    }

    @Test
    public void testStartsEmpty() {
        assertFalse(SpanSinkRegistry.getSpanSinks().iterator().hasNext());
    }

    @Test
    public void testAddOne() {
        SpanSink sink = mock(SpanSink.class);
        SpanSinkRegistry.register(sink);

        ImmutableList<SpanSink> sinks = ImmutableList.copyOf(SpanSinkRegistry.getSpanSinks());
        assertEquals(1, sinks.size());
        assertTrue(sinks.contains(sink));
    }

    @Test
    public void testAddTwo() {
        SpanSink sinkOne = mock(SpanSink.class);
        SpanSink sinkTwo = mock(SpanSink.class);

        assertFalse(sinkOne.equals(sinkTwo));

        SpanSinkRegistry.register(sinkOne);
        SpanSinkRegistry.register(sinkTwo);

        ImmutableList<SpanSink> sinks = ImmutableList.copyOf(SpanSinkRegistry.getSpanSinks());
        assertEquals(2, sinks.size());

        assertEquals(ImmutableList.of(sinkOne, sinkTwo), sinks);
    }

    @Test
    public void testIsClearable() {
        SpanSink sink = mock(SpanSink.class);
        SpanSinkRegistry.register(sink);

        assertTrue(SpanSinkRegistry.getSpanSinks().iterator().hasNext());

        SpanSinkRegistry.clear();

        assertFalse(SpanSinkRegistry.getSpanSinks().iterator().hasNext());
    }

    // todo - yeuch.
    @Test(timeout = 5000)
    public void testRaceCondition() throws InterruptedException {
        int parties = 2;

        final AtomicInteger barrierGenerations = new AtomicInteger();
        final CyclicBarrier barrier = new CyclicBarrier(parties, new Runnable() {
            @Override
            public void run() {
                int generation = barrierGenerations.incrementAndGet();
            }
        });

        final ArrayBlockingQueue<Throwable> caught = new ArrayBlockingQueue<>(1);
        final AtomicBoolean running = new AtomicBoolean(true);

        // We start 2 threads with a cyclic barrier repeatedly and wait a collision with at least one
        long endBy = System.currentTimeMillis() + 5000;
        for (int i = 0; i < parties; i++) {
            new CyclicBarrierThread(barrier, caught, running, endBy).start();
        }

        //noinspection ThrowableResultOfMethodCallIgnored
        assertEquals("Failed to add new SpanSink, concurrent add", caught.poll(5000, TimeUnit.MILLISECONDS).getMessage());

        int sinks = ImmutableList.copyOf(SpanSinkRegistry.getSpanSinks()).size();
        int totalAdds = barrierGenerations.get() * 2;
        assertTrue(sinks + " < " + totalAdds + "?", totalAdds > sinks);
    }

    private static class CyclicBarrierThread extends Thread {
        private final CyclicBarrier barrier;
        private final ArrayBlockingQueue<Throwable> caught;
        private final AtomicBoolean running;
        private final long endBy;

        public CyclicBarrierThread(CyclicBarrier barrier, ArrayBlockingQueue<Throwable> caught, AtomicBoolean running, long endBy) {
            this.barrier = barrier;
            this.caught = caught;
            this.running = running;
            this.endBy = endBy;
        }

        @Override
        public void run() {
            // we have an upper bound on how long we run...
            while (!timedOut() && running.get()) {
                try {
                    barrier.await();
                    SpanSinkRegistry.register(mock(SpanSink.class));
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                } catch (Throwable t) {
                    caught.offer(t);
                    running.compareAndSet(true, false); // allow other threads to end
                }
            }
        }

        private boolean timedOut() {
            return System.currentTimeMillis() > endBy;
        }
    }
}