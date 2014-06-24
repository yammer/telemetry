package com.yammer.telemetry.agent.handlers;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.telemetry.test.TransformedTest;
import com.yammer.telemetry.tracing.*;
import javassist.ClassPool;
import javassist.CtClass;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static com.yammer.telemetry.test.TelemetryTestHelpers.runTransformed;
import static org.junit.Assert.*;

public class MetricsRegistryHandlerTest {
    private MetricsRegistryHandler handler = new MetricsRegistryHandler();

    @After
    public void clearSpanSinkRegistry() {
        SpanSinkRegistry.clear();
    }

    @Test
    public void testNothingForNonMetricsRegistryClass() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get("java.lang.String");
        assertFalse(handler.transformed(ctClass, cp));
    }

    @Test
    public void testTransformsMetricsRegistryClass() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get("com.yammer.metrics.core.MetricsRegistry");
        assertTrue(handler.transformed(ctClass, cp));
    }

    @Test
    public void testUnwrappedTimer() throws Exception {
        MetricsRegistry registry = new MetricsRegistry();
        Timer timer = registry.newTimer(MetricsRegistryHandlerTest.class, "example");

        TimerContext context = timer.time();
        context.stop();

        assertEquals(1, timer.count());
    }

    @Test
    public void testUnwrappedMeter() throws Exception {
        MetricsRegistry registry = new MetricsRegistry();
        Meter meter = registry.newMeter(MetricsRegistryHandlerTest.class, "example", "things", TimeUnit.SECONDS);

        meter.mark(10);

        assertEquals(10, meter.count());
    }

    @Test
    public void testRunTransformedTests() throws Exception {
        runTransformed(TransformedTests.class, handler);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class TransformedTests {
        @Before
        public static void clearSpanSinkRegistry() {
            SpanSinkRegistry.clear();
        }

        @TransformedTest
        public static void testRecordsSpanAnnotationsAroundCallable() throws Exception {
            InMemorySpanSinkSource sink = new InMemorySpanSinkSource();
            SpanSinkRegistry.register(sink);

            try (Span trace = SpanHelper.startTrace("trace")) {
                Timer timer = Metrics.newTimer(TransformedTests.class, "testRecordsSpanAnnotationsAroundCallable");
                timer.time(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        return null;
                    }
                });
            }

            assertEquals(1, sink.recordedTraceCount());
            Trace trace = sink.getTraces().iterator().next();
            SpanData root = trace.getRoot();

            assertEquals("trace", root.getName());
            List<AnnotationData> annotations = trace.getAnnotations(root.getSpanId());
            assertEquals(2, annotations.size());

            AnnotationData data0 = annotations.get(0);
            assertEquals("Start Timer", data0.getName());
            assertEquals("\"com.yammer.telemetry.agent.handlers\":type=\"TransformedTests\",name=\"testRecordsSpanAnnotationsAroundCallable\"", data0.getMessage());

            AnnotationData data1 = annotations.get(1);
            assertEquals("Stop Timer", data1.getName());
            assertEquals("\"com.yammer.telemetry.agent.handlers\":type=\"TransformedTests\",name=\"testRecordsSpanAnnotationsAroundCallable\"", data1.getMessage());
        }

        @TransformedTest
        public static void testRecordsSpanAnnotationsAroundTimerContext() throws Exception {
            InMemorySpanSinkSource sink = new InMemorySpanSinkSource();
            SpanSinkRegistry.register(sink);

            try (Span trace = SpanHelper.startTrace("trace")) {
                Timer timer = Metrics.newTimer(TransformedTests.class, "testRecordsSpanAnnotationsAroundTimerContext");
                TimerContext timerContext = timer.time();
                timerContext.stop();
            }

            assertEquals(1, sink.recordedTraceCount());
            Trace trace = sink.getTraces().iterator().next();
            SpanData root = trace.getRoot();

            assertEquals("trace", root.getName());
            List<AnnotationData> annotations = trace.getAnnotations(root.getSpanId());
            assertEquals(2, annotations.size());

            AnnotationData data0 = annotations.get(0);
            assertEquals("Start Timer", data0.getName());
            assertEquals("\"com.yammer.telemetry.agent.handlers\":type=\"TransformedTests\",name=\"testRecordsSpanAnnotationsAroundTimerContext\"", data0.getMessage());

            AnnotationData data1 = annotations.get(1);
            assertEquals("Stop Timer", data1.getName());
            assertEquals("\"com.yammer.telemetry.agent.handlers\":type=\"TransformedTests\",name=\"testRecordsSpanAnnotationsAroundTimerContext\"", data1.getMessage());
        }

        @TransformedTest
        public static void testRecordsSpanAnnotationAroundMeter() throws Exception {
            InMemorySpanSinkSource sink = new InMemorySpanSinkSource();
            SpanSinkRegistry.register(sink);

            try (Span trace = SpanHelper.startTrace("trace")) {
                Meter meter = Metrics.newMeter(TransformedTests.class, "testRecordsSpanAnnotationAroundMeter", "tests", TimeUnit.MILLISECONDS);
                meter.mark(13);
            }

            assertEquals(1, sink.recordedTraceCount());
            Trace trace = sink.getTraces().iterator().next();
            SpanData root = trace.getRoot();
            assertEquals("trace", root.getName());
            List<AnnotationData> annotations = trace.getAnnotations(root.getSpanId());
            assertEquals(1, annotations.size());
            AnnotationData data = annotations.get(0);
            assertEquals("Mark Meter: \"com.yammer.telemetry.agent.handlers\":type=\"TransformedTests\",name=\"testRecordsSpanAnnotationAroundMeter\"", data.getName());
            assertEquals("13", data.getMessage());
        }

        @TransformedTest
        public static void testRecordsSpanAnnotationsAroundCounter() throws Exception {
            InMemorySpanSinkSource sink = new InMemorySpanSinkSource();
            SpanSinkRegistry.register(sink);

            try (Span trace = SpanHelper.startTrace("trace")) {
                Counter meter = Metrics.newCounter(TransformedTests.class, "testRecordsSpanAnnotationsAroundCounter");
                meter.inc(13);
                meter.dec(10);
                meter.clear();
            }

            assertEquals(1, sink.recordedTraceCount());
            Trace trace = sink.getTraces().iterator().next();
            SpanData root = trace.getRoot();
            assertEquals("trace", root.getName());
            List<AnnotationData> annotations = trace.getAnnotations(root.getSpanId());
            assertEquals(3, annotations.size());

            AnnotationData data0 = annotations.get(0);
            assertEquals("Inc Counter: \"com.yammer.telemetry.agent.handlers\":type=\"TransformedTests\",name=\"testRecordsSpanAnnotationsAroundCounter\"", data0.getName());
            assertEquals("13", data0.getMessage());

            AnnotationData data1 = annotations.get(1);
            assertEquals("Dec Counter: \"com.yammer.telemetry.agent.handlers\":type=\"TransformedTests\",name=\"testRecordsSpanAnnotationsAroundCounter\"", data1.getName());
            assertEquals("10", data1.getMessage());

            AnnotationData data3 = annotations.get(2);
            assertEquals("Cleared Counter: \"com.yammer.telemetry.agent.handlers\":type=\"TransformedTests\",name=\"testRecordsSpanAnnotationsAroundCounter\"", data3.getName());
            assertEquals(null, data3.getMessage());
        }

    }
}
