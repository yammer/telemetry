package com.yammer.telemetry.tracing;

import com.google.common.base.Optional;
import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class AsynchronousSpanSinkTest {
    @Test
    public void testSubmitsExpectedRecordTasksToExecutor() {
        ExecutorService executor = mock(ExecutorService.class);

        AsynchronousSpanSink.JobFactory jobFactory = mock(AsynchronousSpanSink.JobFactory.class);
        SpanSink sink = new AsynchronousSpanSink(executor, jobFactory);

        SpanData spanData = new FakeSpanData();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
            }
        };

        when(jobFactory.createJob(eq(spanData))).thenReturn(runnable);

        sink.record(spanData);

        verify(jobFactory).createJob(eq(spanData));
        verify(executor).execute(eq(runnable));
    }

    @Test
    public void testSubmitsExpectedAnnotationTasksToExecutor() {
        ExecutorService executor = mock(ExecutorService.class);

        AsynchronousSpanSink.JobFactory jobFactory = mock(AsynchronousSpanSink.JobFactory.class);
        SpanSink sink = new AsynchronousSpanSink(executor, jobFactory);

        AnnotationData annotationData = new FakeAnnotationData();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
            }
        };

        when(jobFactory.createJob(BigInteger.ONE, BigInteger.TEN, annotationData)).thenReturn(runnable);

        sink.recordAnnotation(BigInteger.ONE, BigInteger.TEN, annotationData);

        verify(jobFactory).createJob(BigInteger.ONE, BigInteger.TEN, annotationData);
        verify(executor).execute(eq(runnable));
    }

    @Test
    public void testShutdownWhenLongRunningTask() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        final CountDownLatch interruptedLatch = new CountDownLatch(1);

        AsynchronousSpanSink sink = new AsynchronousSpanSink(executor, new AsynchronousSpanSink.JobFactory() {
            @Override
            public Runnable createJob(SpanData data) {
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            interruptedLatch.countDown();
                            e.printStackTrace();
                        }
                    }
                };
            }

            @Override
            public Runnable createJob(BigInteger traceId, BigInteger spanId, AnnotationData data) {
                throw new UnsupportedOperationException();
            }
        });

        sink.record(new FakeSpanData());

        // there are zero waiting tasks
        assertEquals(0, sink.shutdown(100, TimeUnit.MILLISECONDS));

        assertTrue(interruptedLatch.await(200, TimeUnit.SECONDS));
    }

    @Test
    public void testShutdownWhenLongRunningTaskAndQueuedTasks() throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();

        final CountDownLatch interruptedLatch = new CountDownLatch(1);

        AsynchronousSpanSink sink = new AsynchronousSpanSink(executor, new AsynchronousSpanSink.JobFactory() {
            @Override
            public Runnable createJob(SpanData data) {
                return new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Thread.sleep(5000);
                        } catch (InterruptedException e) {
                            interruptedLatch.countDown();
                            e.printStackTrace();
                        }
                    }
                };
            }

            @Override
            public Runnable createJob(BigInteger traceId, BigInteger spanId, AnnotationData data) {
                throw new UnsupportedOperationException();
            }
        });

        sink.record(new FakeSpanData());
        sink.record(new FakeSpanData());
        sink.record(new FakeSpanData());

        assertEquals(2, sink.shutdown(100, TimeUnit.MILLISECONDS));

        assertTrue(interruptedLatch.await(200, TimeUnit.SECONDS));
    }

    private static class FakeSpanData implements SpanData {
        private long startTime = System.nanoTime();

        @Override
        public BigInteger getTraceId() {
            return BigInteger.ONE;
        }

        @Override
        public BigInteger getId() {
            return BigInteger.TEN;
        }

        @Override
        public Optional<BigInteger> getParentId() {
            return Optional.absent();
        }

        @Override
        public String getName() {
            return "Some Name";
        }

        @Override
        public long getStartTimeNanos() {
            return startTime;
        }

        @Override
        public long getDuration() {
            return 100;
        }
    }

    private static class FakeAnnotationData implements AnnotationData {
        private long startTime = System.nanoTime();

        @Override
        public long getStartTimeNanos() {
            return startTime;
        }

        @Override
        public String getName() {
            return "A Name";
        }

        @Override
        public String getMessage() {
            return "A Message";
        }
    }
}
