package com.yammer.telemetry.agent.handlers;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.core.*;
import com.yammer.telemetry.agent.TelemetryTransformer;
import com.yammer.telemetry.agent.test.TransformingClassLoader;
import com.yammer.telemetry.tracing.InMemorySpanSinkSource;
import com.yammer.telemetry.tracing.Span;
import com.yammer.telemetry.tracing.SpanSinkRegistry;
import com.yammer.telemetry.tracing.Trace;
import javassist.ClassPool;
import javassist.CtClass;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class MetricsRegistryHandlerTest {
    private MetricsRegistryHandler handler = new MetricsRegistryHandler();

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
    public void testWrapsTimers() throws Exception {
        TelemetryTransformer transformer = new TelemetryTransformer();
        transformer.addHandler(handler);

        try (TransformingClassLoader loader = new TransformingClassLoader(Metrics.class, transformer)) {
            Class<?> spanSinkRegistry = loader.loadClass("com.yammer.telemetry.tracing.SpanSinkRegistry");
            Class<?> inMemorySpanSinkSource = loader.loadClass("com.yammer.telemetry.tracing.InMemorySpanSinkSource");
            Class<?> spanSink = loader.loadClass("com.yammer.telemetry.tracing.SpanSink");

            Object sink = inMemorySpanSinkSource.newInstance();

            Method register = spanSinkRegistry.getDeclaredMethod("register", spanSink);
            register.invoke(null, sink);

            try (Span trace = Span.startTrace("trace")) {
                Class<?> metricsRegistryClass = loader.loadClass("com.yammer.metrics.core.MetricsRegistry");

                Method method = metricsRegistryClass.getDeclaredMethod("newTimer", Class.class, String.class);
                Object timer = method.invoke(metricsRegistryClass.newInstance(), MetricsRegistryHandlerTest.class, "example");

                Method timeMethod = timer.getClass().getMethod("time");
                Object context = timeMethod.invoke(timer);

                Method stopMethod = context.getClass().getMethod("stop");
                stopMethod.invoke(context);
            }

            Method getTraces = inMemorySpanSinkSource.getDeclaredMethod("getTraces");
            Collection traces = (Collection)getTraces.invoke(sink);
            assertEquals(1, traces.size());

            Object trace1 = traces.iterator().next();
            System.out.println("trace1 = " + trace1);
        }
    }

    @Test
    public void testUnwrappedMeter() throws Exception {
        MetricsRegistry registry = new MetricsRegistry();
        Meter meter = registry.newMeter(MetricsRegistryHandlerTest.class, "example", "things", TimeUnit.SECONDS);

        meter.mark(10);

        assertEquals(10, meter.count());
    }
}
