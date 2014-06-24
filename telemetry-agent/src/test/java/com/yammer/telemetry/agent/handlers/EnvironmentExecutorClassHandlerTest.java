package com.yammer.telemetry.agent.handlers;

import com.google.common.base.Function;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.telemetry.test.TransformedTest;
import com.yammer.telemetry.tracing.*;
import javassist.ClassPool;
import javassist.CtClass;
import org.junit.After;
import org.junit.Test;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.*;

import static com.yammer.telemetry.test.TelemetryTestHelpers.runTransformed;
import static org.junit.Assert.*;

public class EnvironmentExecutorClassHandlerTest {
    private EnvironmentExecutorClassHandler handler = new EnvironmentExecutorClassHandler();

    @After
    public void clearSpanSinkRegistry() {
        SpanSinkRegistry.clear();
    }


    @Test
    public void testNothingForUnrelatedClasses() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get("java.lang.String");
        assertFalse(handler.transformed(ctClass, cp));
    }

    @Test
    public void testAltersEnvironmentsClass() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get("com.yammer.dropwizard.config.Environment");
        assertTrue(handler.transformed(ctClass, cp));
    }

    @Test
    public void testRunTransformedTests() throws Exception {
        runTransformed(TransformedTests.class, handler);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class TransformedTests {
        @TransformedTest
        public void testManagedExecutorServiceIsInstrumented() throws Exception {
            Environment environment = new Environment("test", new Configuration(), new ObjectMapperFactory(), new Validator());
            ExecutorService executorService = environment.managedExecutorService("test-thread-%s", 1, 1, 1, TimeUnit.MINUTES);

            assertTrue(executorService instanceof InstrumentedThreadPoolExecutor);
        }

        @TransformedTest
        public void testManagedScheduledExecutorServiceIsInstrumented() throws Exception {
            Environment environment = new Environment("test", new Configuration(), new ObjectMapperFactory(), new Validator());
            ExecutorService executorService = environment.managedScheduledExecutorService("test-scheduled-thread-%s", 1);

            assertTrue(executorService instanceof InstrumentedScheduledThreadPoolExecutor);
        }

        @TransformedTest
        public void testThreadPoolNotPollutedWithInvalidSpanContexts() throws Exception {
            Environment environment = new Environment("test", new Configuration(), new ObjectMapperFactory(), new Validator());
            final ExecutorService executorService = environment.managedExecutorService("test-thread-%s", 1, 1, 0, TimeUnit.SECONDS);

            try (CapturingExceptionHandler eh = new CapturingExceptionHandler()) {
                Runnable noop = new Runnable() {
                    @Override
                    public void run() {

                    }
                };

                Span trace = SpanHelper.startTrace("Trace");

                executorService.submit(noop);
                trace.end();

                Future<?> second = executorService.submit(noop);
                second.get(); // wait for the second task to complete

                assertNull(eh.pollUncaught(100, TimeUnit.MILLISECONDS));
            }
        }

        @TransformedTest
        public void testScheduledThreadPoolNotPollutedWithInvalidSpanContexts() throws Exception {
            Environment environment = new Environment("test", new Configuration(), new ObjectMapperFactory(), new Validator());
            final ScheduledExecutorService executorService = environment.managedScheduledExecutorService("test-scheduled-thread-%s", 1);

            try (CapturingExceptionHandler eh = new CapturingExceptionHandler()) {
                Runnable noop = new Runnable() {
                    @Override
                    public void run() {

                    }
                };

                Span trace = SpanHelper.startTrace("Trace");

                executorService.schedule(noop, 1, TimeUnit.MILLISECONDS);
                trace.end();

                Future<?> second = executorService.schedule(noop, 1, TimeUnit.MILLISECONDS);
                second.get(); // wait for the second task to complete

                assertNull(eh.pollUncaught(100, TimeUnit.MILLISECONDS));
            }
        }

        @TransformedTest
        public void testRunnableAnnotationsAreRecordedUnderExistingTraceForManagedScheduledExecutor() throws Exception {
            InMemorySpanSinkSource sink = new InMemorySpanSinkSource();
            SpanSinkRegistry.register(sink);

            Span rootSpan = SpanHelper.attachSpan(BigInteger.ONE, BigInteger.TEN, "trace");

            final ArrayBlockingQueue<Span> latch = new ArrayBlockingQueue<>(1);
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    try (Span span = SpanHelper.startSpan("Offer")) {
                        latch.offer(span);
                    }
                }
            };
            try (Span ignored = rootSpan) {
                Environment environment = new Environment("test", new Configuration(), new ObjectMapperFactory(), new Validator());
                ScheduledExecutorService executorService = environment.managedScheduledExecutorService("test-scheduled-thread-%s", 1);
                executorService.schedule(task, 10, TimeUnit.MILLISECONDS);
            }

            final Span innerSpan = latch.poll(100, TimeUnit.MILLISECONDS);

            assertEquals(1, sink.recordedTraceCount());

            Trace trace = sink.getTrace(BigInteger.ONE);
            assertNotNull(trace);

            List<AnnotationData> rootAnnotations = trace.getAnnotations(rootSpan.getSpanId());
            assertEquals(3, rootAnnotations.size());
            ImmutableList<String> annotationNames = ImmutableList.copyOf(Iterables.transform(rootAnnotations, new Function<AnnotationData, String>() {
                @Override
                public String apply(AnnotationData input) {
                    return input.getName();
                }
            }));
            ImmutableList<String> annotationValues = ImmutableList.copyOf(Iterables.transform(rootAnnotations, new Function<AnnotationData, String>() {
                @Override
                public String apply(AnnotationData input) {
                    return input.getMessage();
                }
            }));
            assertTrue(annotationNames.containsAll(ImmutableList.of("Scheduled Task", "Before", "After")));
            String expectedTaskName = task.getClass().getName();
            assertTrue(annotationValues.containsAll(ImmutableList.of(expectedTaskName, expectedTaskName, expectedTaskName)));

            assertEquals(1, trace.getChildren(rootSpan.getSpanId()).size());
            SpanData spanData = trace.getChildren(rootSpan.getSpanId()).get(0);
            assertEquals("Offer", spanData.getName());
            assertEquals(BigInteger.ONE, spanData.getTraceId());
            assertEquals(Optional.of(BigInteger.TEN), spanData.getParentSpanId());
        }

        @TransformedTest
        public void testCallableAnnotationsAreRecordedUnderExistingTraceForManagedScheduledExecutor() throws Exception {
            InMemorySpanSinkSource sink = new InMemorySpanSinkSource();
            SpanSinkRegistry.register(sink);

            Span rootSpan = SpanHelper.attachSpan(BigInteger.ONE, BigInteger.TEN, "trace");

            final ArrayBlockingQueue<Span> latch = new ArrayBlockingQueue<>(1);
            Callable<Boolean> task = new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    try (Span span = SpanHelper.startSpan("Offer")) {
                        return latch.offer(span);
                    }
                }
            };
            try (Span ignored = rootSpan) {
                Environment environment = new Environment("test", new Configuration(), new ObjectMapperFactory(), new Validator());
                ScheduledExecutorService executorService = environment.managedScheduledExecutorService("test-scheduled-thread-%s", 1);
                executorService.schedule(task, 10, TimeUnit.MILLISECONDS);
            }

            final Span innerSpan = latch.poll(100, TimeUnit.MILLISECONDS);

            assertEquals(1, sink.recordedTraceCount());

            Trace trace = sink.getTrace(BigInteger.ONE);
            assertNotNull(trace);

            List<AnnotationData> rootAnnotations = trace.getAnnotations(rootSpan.getSpanId());
            assertEquals(3, rootAnnotations.size());
            ImmutableList<String> annotationNames = ImmutableList.copyOf(Iterables.transform(rootAnnotations, new Function<AnnotationData, String>() {
                @Override
                public String apply(AnnotationData input) {
                    return input.getName();
                }
            }));
            ImmutableList<String> annotationValues = ImmutableList.copyOf(Iterables.transform(rootAnnotations, new Function<AnnotationData, String>() {
                @Override
                public String apply(AnnotationData input) {
                    return input.getMessage();
                }
            }));
            assertTrue(annotationNames.containsAll(ImmutableList.of("Scheduled Task", "Before", "After")));
            String expectedTaskName = task.getClass().getName();
            assertTrue(annotationValues.containsAll(ImmutableList.of(expectedTaskName, expectedTaskName, expectedTaskName)));

            assertEquals(1, trace.getChildren(rootSpan.getSpanId()).size());
            SpanData spanData = trace.getChildren(rootSpan.getSpanId()).get(0);
            assertEquals("Offer", spanData.getName());
            assertEquals(BigInteger.ONE, spanData.getTraceId());
            assertEquals(Optional.of(BigInteger.TEN), spanData.getParentSpanId());
        }

        @TransformedTest
        public void testRunnableAnnotationsAreRecordedUnderExistingTraceForManagedExecutor() throws Exception {
            InMemorySpanSinkSource sink = new InMemorySpanSinkSource();
            SpanSinkRegistry.register(sink);

            Span rootSpan = SpanHelper.attachSpan(BigInteger.ONE, BigInteger.TEN, "trace");

            final ArrayBlockingQueue<Span> latch = new ArrayBlockingQueue<>(1);
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    try (Span span = SpanHelper.startSpan("Offer")) {
                        latch.offer(span);
                    }
                }
            };

            try (Span ignored = rootSpan) {
                Environment environment = new Environment("test", new Configuration(), new ObjectMapperFactory(), new Validator());
                ExecutorService executorService = environment.managedExecutorService("test-scheduled-thread-%s", 1, 1, 1, TimeUnit.SECONDS);

                executorService.submit(task, "SomeResult");
            }

            final Span innerSpan = latch.poll(100, TimeUnit.MILLISECONDS);
            Thread.yield();

            assertEquals(1, sink.getTraces().size());

            Trace trace = sink.getTrace(BigInteger.ONE);
            assertNotNull(trace);

            List<AnnotationData> rootAnnotations = trace.getAnnotations(rootSpan.getSpanId());
            assertEquals(3, rootAnnotations.size());
            ImmutableList<String> annotationNames = ImmutableList.copyOf(Iterables.transform(rootAnnotations, new Function<AnnotationData, String>() {
                @Override
                public String apply(AnnotationData input) {
                    return input.getName();
                }
            }));
            ImmutableList<String> annotationValues = ImmutableList.copyOf(Iterables.transform(rootAnnotations, new Function<AnnotationData, String>() {
                @Override
                public String apply(AnnotationData input) {
                    return input.getMessage();
                }
            }));
            assertTrue(annotationNames.containsAll(ImmutableList.of("Task", "Before", "After")));
            String expectedTaskName = task.getClass().getName() + ":SomeResult"; // return value is appended
            assertTrue(annotationValues.containsAll(ImmutableList.of(expectedTaskName, expectedTaskName, expectedTaskName)));


            assertEquals(1, trace.getChildren(rootSpan.getSpanId()).size());
            SpanData spanData = trace.getChildren(rootSpan.getSpanId()).get(0);
            assertEquals("Offer", spanData.getName());
            assertEquals(BigInteger.ONE, spanData.getTraceId());
            assertEquals(Optional.of(BigInteger.TEN), spanData.getParentSpanId());
        }

        @TransformedTest
        public void testCallableAnnotationsAreRecordedUnderExistingTraceForManagedExecutor() throws Exception {
            InMemorySpanSinkSource sink = new InMemorySpanSinkSource();
            SpanSinkRegistry.register(sink);

            Span rootSpan = SpanHelper.attachSpan(BigInteger.ONE, BigInteger.TEN, "trace");

            final ArrayBlockingQueue<Span> latch = new ArrayBlockingQueue<>(1);
            final Runnable task = new Runnable() {
                @Override
                public void run() {
                    try (Span span = SpanHelper.startSpan("Offer")) {
                        latch.offer(span);
                    }
                }
            };

            try (Span ignored = rootSpan) {
                Environment environment = new Environment("test", new Configuration(), new ObjectMapperFactory(), new Validator());
                ExecutorService executorService = environment.managedExecutorService("test-scheduled-thread-%s", 1, 1, 1, TimeUnit.SECONDS);

                executorService.submit(task, "SomeResult");
            }

            final Span innerSpan = latch.poll(100, TimeUnit.MILLISECONDS);

            assertEquals(1, sink.recordedTraceCount());

            Trace trace = sink.getTrace(BigInteger.ONE);
            assertNotNull(trace);

            List<AnnotationData> rootAnnotations = trace.getAnnotations(rootSpan.getSpanId());
            assertEquals(3, rootAnnotations.size());
            ImmutableList<String> annotationNames = ImmutableList.copyOf(Iterables.transform(rootAnnotations, new Function<AnnotationData, String>() {
                @Override
                public String apply(AnnotationData input) {
                    return input.getName();
                }
            }));
            ImmutableList<String> annotationValues = ImmutableList.copyOf(Iterables.transform(rootAnnotations, new Function<AnnotationData, String>() {
                @Override
                public String apply(AnnotationData input) {
                    return input.getMessage();
                }
            }));
            assertTrue(annotationNames.containsAll(ImmutableList.of("Task", "Before", "After")));
            String expectedTaskName = task.getClass().getName() + ":SomeResult"; // return value is appended
            assertTrue(annotationValues.containsAll(ImmutableList.of(expectedTaskName, expectedTaskName, expectedTaskName)));

            assertEquals(1, trace.getChildren(rootSpan.getSpanId()).size());
            SpanData spanData = trace.getChildren(rootSpan.getSpanId()).get(0);
            assertEquals("Offer", spanData.getName());
            assertEquals(BigInteger.ONE, spanData.getTraceId());
            assertEquals(Optional.of(BigInteger.TEN), spanData.getParentSpanId());
        }

        private static class CapturingExceptionHandler implements Thread.UncaughtExceptionHandler, AutoCloseable {
            private final BlockingDeque<String> uncaughtExceptions = new LinkedBlockingDeque<>();
            private final Thread.UncaughtExceptionHandler prior;

            public CapturingExceptionHandler() {
                prior = Thread.getDefaultUncaughtExceptionHandler();
                Thread.setDefaultUncaughtExceptionHandler(this);
            }

            @Override
            public void uncaughtException(Thread t, Throwable e) {
                StringWriter sw = new StringWriter();
                PrintWriter writer = new PrintWriter(sw);
                writer.printf("Thread: %s%n", t.getName());
                e.printStackTrace(writer);
                uncaughtExceptions.add(sw.toString());
            }

            @Override
            public void close() throws Exception {
                Thread.setDefaultUncaughtExceptionHandler(prior);
            }

            public String pollUncaught(int timeout, TimeUnit timeUnit) throws InterruptedException {
                return uncaughtExceptions.poll(timeout, timeUnit);
            }
        }
    }
}
