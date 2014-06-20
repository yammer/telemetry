package com.yammer.telemetry.tracing;

import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class AsynchronousSpanSinkTest {
    @Test
    public void testSubmitsExpectedRecordTasksToExecutor() {
        ExecutorService executor = mock(ExecutorService.class);

        AsynchronousSpanSink.JobFactory jobFactory = mock(AsynchronousSpanSink.JobFactory.class);
        SpanSink sink = new AsynchronousSpanSink(executor, jobFactory);

        SpanData spanData = new BeanSpanData();
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
                        }
                    }
                };
            }

        });

        sink.record(new BeanSpanData());

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
                        }
                    }
                };
            }

        });

        sink.record(new BeanSpanData());
        sink.record(new BeanSpanData());
        sink.record(new BeanSpanData());

        assertEquals(2, sink.shutdown(100, TimeUnit.MILLISECONDS));

        assertTrue(interruptedLatch.await(200, TimeUnit.SECONDS));
    }

}
