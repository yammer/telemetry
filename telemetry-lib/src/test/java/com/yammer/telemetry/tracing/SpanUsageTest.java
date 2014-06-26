package com.yammer.telemetry.tracing;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class SpanUsageTest {
    @Rule
    public SpanContextRule spanContextRule = new SpanContextRule();

    private InMemorySpanSinkSource sink;
    private Sampling defaultSampler;

    @Before
    public void setUp() throws Exception {
        sink = new InMemorySpanSinkSource();
        SpanSinkRegistry.register(sink);
        defaultSampler = SpanHelper.getSampler();
    }

    @After
    public void tearDown() {
        SpanSinkRegistry.clear();
        SpanHelper.setSampler(defaultSampler);
    }

    @Test
    public void testAttachSpan() {
        Trace trace = testSpan(new Strategy() {
            @Override
            public Span createSpan() {
                return SpanHelper.attachSpan(BigInteger.ONE, BigInteger.TEN, "wubba");
            }
        });

        SpanData rootSpan = trace.getRoot();
        assertEquals("wubba", rootSpan.getName());
        assertNotNull(rootSpan.getHost());

        List<AnnotationData> annotations = trace.getAnnotations(BigInteger.TEN);
        assertEquals(3, annotations.size());

        assertEquals(ImmutableList.of(AnnotationNames.SERVER_RECEIVED, AnnotationNames.SERVICE_NAME + "_testing", AnnotationNames.SERVER_SENT), ImmutableList.copyOf(Iterables.transform(annotations, new Function<AnnotationData, String>() {
            @Override
            public String apply(AnnotationData input) {
                String message = input.getMessage() == null ? "" : "_" + input.getMessage();
                return input.getName() + message;
            }
        })));
    }

    @Test
    public void testStartSpan() {
        Trace trace = testSpan(new Strategy() {
            @Override
            public Span createSpan() {
                return SpanHelper.startSpan("Tracing");
            }
        });

        assertNotNull(trace.getRoot());

        List<AnnotationData> annotations = trace.getAnnotations(trace.getRoot().getSpanId());
        assertEquals(3, annotations.size());

        assertEquals(ImmutableList.of(AnnotationNames.SERVER_RECEIVED, AnnotationNames.SERVICE_NAME + "_testing", AnnotationNames.SERVER_SENT), ImmutableList.copyOf(Iterables.transform(annotations, new Function<AnnotationData, String>() {
            @Override
            public String apply(AnnotationData input) {
                String message = input.getMessage() == null ? "" : "_" + input.getMessage();
                return input.getName() + message;
            }
        })));
    }

    @Test(expected = NullPointerException.class)
    public void testCannotAttachWithoutTraceId() {
        SpanHelper.attachSpan(null, BigInteger.ONE, "name");
    }

    @Test(expected = NullPointerException.class)
    public void testCannotAttachWithoutSpanId() {
        SpanHelper.attachSpan(BigInteger.ONE, null, "name");
    }

    @Test(expected = NullPointerException.class)
    public void testCannotAttachWithoutTraceAndSpanIds() {
        SpanHelper.attachSpan(null, null, "name");
    }

    @Test
    public void testDefaultSamplerIsOn() {
        assertEquals(Sampling.ON, defaultSampler);
    }

    @Test
    public void testStartTraceRecordsWhenSamplingIsOn() {
        SpanHelper.setSampler(Sampling.ON);
        Span trace = SpanHelper.startTrace("Foof");
        trace.end();

        assertFalse(sink.getTraces().isEmpty());
    }

    @Test
    public void testStartTraceRecordsNothingWhenSamplingIsOff() {
        SpanHelper.setSampler(Sampling.OFF);
        Span trace = SpanHelper.startTrace("Foof");
        trace.end();

        assertTrue(sink.getTraces().isEmpty());
    }

    @Test
    public void testStartSpanRecordsWhenSamplingIsOn() {
        SpanHelper.setSampler(Sampling.ON);
        Span trace = SpanHelper.startSpan("Foof");
        trace.end();

        assertFalse(sink.getTraces().isEmpty());
    }

    @Test
    public void testStartSpanRecordsNothingWhenSamplingIsOff() {
        SpanHelper.setSampler(Sampling.OFF);
        Span trace = SpanHelper.startSpan("Foof");
        trace.end();

        assertTrue(sink.getTraces().isEmpty());
    }

    @Test
    public void testStartSpanWithTraceAndSpanIdRecordsWhenSamplingIsOn() {
        SpanHelper.setSampler(Sampling.ON);
        Span trace = SpanHelper.startSpan(BigInteger.ONE, BigInteger.TEN, "Foof");
        trace.end();

        assertFalse(sink.getTraces().isEmpty());
        Trace recordedTrace = sink.getTraces().iterator().next();
        assertEquals(BigInteger.ONE, recordedTrace.getTraceId());
        assertEquals("Foof", recordedTrace.getChildren(BigInteger.TEN).get(0).getName());
    }

    @Test
    public void testStartSpanWithTraceAndSpanIdRecordsEvenWhenSamplingIsOff() {
        SpanHelper.setSampler(Sampling.OFF);
        Span trace = SpanHelper.startSpan(BigInteger.ONE, BigInteger.TEN, "Foof");
        trace.end();

        assertFalse(sink.getTraces().isEmpty());
        Trace recordedTrace = sink.getTraces().iterator().next();
        assertEquals(BigInteger.ONE, recordedTrace.getTraceId());
        assertEquals("Foof", recordedTrace.getChildren(BigInteger.TEN).get(0).getName());
    }

    @Test
    public void testSpanRecordsAfterAttachSpanIfSamplingIsOn() {
        SpanHelper.setSampler(Sampling.ON);
        Span foof = SpanHelper.attachSpan(BigInteger.ONE, BigInteger.TEN, "Foof");
        Span subFoof = SpanHelper.startSpan("SubFoof");
        subFoof.addAnnotation("A");
        subFoof.end();
        foof.end();

        assertEquals(1, sink.recordedTraceCount());
        Trace trace = sink.getTrace(BigInteger.ONE);
        SpanData rootSpan = trace.getRoot();
        assertNotNull(rootSpan);
        assertEquals(BigInteger.ONE, rootSpan.getTraceId());
        assertEquals(BigInteger.TEN, rootSpan.getSpanId());
        assertEquals("Foof", rootSpan.getName());
        assertNotNull(rootSpan.getHost());
        assertEquals(ImmutableList.<SpanData>of(subFoof), trace.getChildren(foof.getSpanId()));
    }

    @Test
    public void testSpanRecordsAfterAttachSpanEvenIfSamplingIsOff() {
        SpanHelper.setSampler(Sampling.OFF);
        Span foof = SpanHelper.attachSpan(BigInteger.ONE, BigInteger.TEN, "Foof");
        Span subFoof = SpanHelper.startSpan("SubFoof");
        subFoof.addAnnotation("A");
        subFoof.end();
        foof.end();

        assertEquals(1, sink.recordedTraceCount());
        Trace trace = sink.getTrace(BigInteger.ONE);
        SpanData rootSpan = trace.getRoot();
        assertEquals("Foof", rootSpan.getName());
        assertNotNull(rootSpan.getHost());
        assertEquals(ImmutableList.<SpanData>of(subFoof), trace.getChildren(foof.getSpanId()));
    }

    @Test
    public void testSpanRecordsAfterStartTraceIfSamplingIsOn() {
        SpanHelper.setSampler(Sampling.ON);
        Span foof = SpanHelper.startTrace("Foof");
        Span subFoof = SpanHelper.startSpan("SubFoof");
        subFoof.addAnnotation("A");
        subFoof.end();
        foof.end();

        assertEquals(1, sink.recordedTraceCount());
        Trace trace = sink.getTrace(foof.getTraceId());
        assertEquals(foof, trace.getRoot());
        assertEquals(ImmutableList.<SpanData>of(subFoof), trace.getChildren(foof.getSpanId()));
    }

    @Test
    public void testSpanDoesNotRecordAfterStartTraceIfSamplingIsOff() {
        SpanHelper.setSampler(Sampling.OFF);
        Span foof = SpanHelper.startTrace("Foof");
        Span subFoof = SpanHelper.startSpan("SubFoof");
        subFoof.addAnnotation("A");
        subFoof.end();
        foof.end();

        assertEquals(0, sink.recordedTraceCount());
        Trace trace = sink.getTrace(foof.getTraceId());
        assertNull(trace);
    }

    @Test
    public void testAnnotationRecordedAfterSpanEnded() {
        SpanHelper.setSampler(Sampling.ON);
        Span trace = SpanHelper.startTrace("trace");
        trace.addAnnotation("During");
        trace.end();
        trace.addAnnotation("After");

        assertEquals(1, sink.recordedTraceCount());
        Trace theTrace = sink.getTrace(trace.getTraceId());
        System.out.println(theTrace);
    }

    private Trace testSpan(Strategy strategy) {
        final AtomicReference<BigInteger> traceId = new AtomicReference<>();
        final AtomicReference<BigInteger> spanId = new AtomicReference<>();
        try (Span span = strategy.createSpan()) {
            traceId.set(span.getTraceId());
            spanId.set(span.getSpanId());

            span.addAnnotation(AnnotationNames.SERVER_RECEIVED);
            span.addAnnotation(AnnotationNames.SERVICE_NAME, "testing");
            SpanHelper.startSpan("here").end();
            SpanHelper.startSpan("there").end();
            span.addAnnotation(AnnotationNames.SERVER_SENT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, sink.recordedTraceCount());
        assertEquals(traceId.get(), sink.getTraces().iterator().next().getTraceId());

        Trace trace = sink.getTrace(traceId.get());

        List<SpanData> children = trace.getChildren(spanId.get());
        assertEquals(2, children.size());
        assertEquals(ImmutableList.of("here", "there"), ImmutableList.copyOf(Iterables.transform(children, new Function<SpanData, String>() {
            @Override
            public String apply(SpanData input) {
                return input.getName();
            }
        })));

        assertEquals(3, trace.getAnnotations(spanId.get()).size());
        return trace;
    }

    private static interface Strategy {
        Span createSpan();
    }
}
