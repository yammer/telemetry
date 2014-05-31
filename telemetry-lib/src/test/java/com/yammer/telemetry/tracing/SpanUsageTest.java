package com.yammer.telemetry.tracing;

import com.google.common.base.Function;
import com.google.common.base.Optional;
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
        defaultSampler = Span.getSampler();
    }

    @After
    public void tearDown() {
        SpanSinkRegistry.clear();
        Span.setSampler(defaultSampler);
    }

    @Test
    public void testAttachSpan() {
        Trace trace = testSpan(new Strategy() {
            @Override
            public Span createSpan() {
                return Span.attachSpan(BigInteger.ONE, BigInteger.TEN, "wubba");
            }
        });

        assertNull(trace.getRoot());

        List<AnnotationData> annotations = trace.getAnnotations(new SpanId(BigInteger.TEN));
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
                return Span.startSpan("Tracing");
            }
        });

        assertNotNull(trace.getRoot());

        List<AnnotationData> annotations = trace.getAnnotations(trace.getRoot());
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
        Span.attachSpan(null, BigInteger.ONE, "name");
    }

    @Test(expected = NullPointerException.class)
    public void testCannotAttachWithoutSpanId() {
        Span.attachSpan(BigInteger.ONE, null, "name");
    }

    @Test(expected = NullPointerException.class)
    public void testCannotAttachWithoutTraceAndSpanIds() {
        Span.attachSpan(null, null, "name");
    }

    @Test
    public void testDefaultSamplerIsOn() {
        assertEquals(Sampling.ON, defaultSampler);
    }

    @Test
    public void testStartTraceRecordsWhenSamplingIsOn() {
        Span.setSampler(Sampling.ON);
        Span trace = Span.startTrace("Foof");
        trace.end();

        assertFalse(sink.getTraces().isEmpty());
    }

    @Test
    public void testStartTraceRecordsNothingWhenSamplingIsOff() {
        Span.setSampler(Sampling.OFF);
        Span trace = Span.startTrace("Foof");
        trace.end();

        assertTrue(sink.getTraces().isEmpty());
    }

    @Test
    public void testStartSpanRecordsWhenSamplingIsOn() {
        Span.setSampler(Sampling.ON);
        Span trace = Span.startSpan("Foof");
        trace.end();

        assertFalse(sink.getTraces().isEmpty());
    }

    @Test
    public void testStartSpanRecordsNothingWhenSamplingIsOff() {
        Span.setSampler(Sampling.OFF);
        Span trace = Span.startSpan("Foof");
        trace.end();

        assertTrue(sink.getTraces().isEmpty());
    }

    @Test
    public void testSpanRecordsAfterAttachSpanIfSamplingIsOn() {
        Span.setSampler(Sampling.ON);
        Span foof = Span.attachSpan(BigInteger.ONE, BigInteger.TEN, "Foof");
        Span subFoof = Span.startSpan("SubFoof");
        subFoof.addAnnotation("A");
        subFoof.end();
        foof.end();

        assertEquals(1, sink.getTraces().size());
        Trace trace = sink.getTrace(BigInteger.ONE);
        assertNull(trace.getRoot());
        assertEquals(ImmutableList.<SpanData>of(subFoof), trace.getChildren(foof));
    }

    @Test
    public void testSpanRecordsAfterAttachSpanEvenIfSamplingIsOff() {
        Span.setSampler(Sampling.OFF);
        Span foof = Span.attachSpan(BigInteger.ONE, BigInteger.TEN, "Foof");
        Span subFoof = Span.startSpan("SubFoof");
        subFoof.addAnnotation("A");
        subFoof.end();
        foof.end();

        assertEquals(1, sink.getTraces().size());
        Trace trace = sink.getTrace(BigInteger.ONE);
        assertNull(trace.getRoot());
        assertEquals(ImmutableList.<SpanData>of(subFoof), trace.getChildren(foof));
    }

    @Test
    public void testSpanRecordsAfterStartTraceIfSamplingIsOn() {
        Span.setSampler(Sampling.ON);
        Span foof = Span.startTrace("Foof");
        Span subFoof = Span.startSpan("SubFoof");
        subFoof.addAnnotation("A");
        subFoof.end();
        foof.end();

        assertEquals(1, sink.getTraces().size());
        Trace trace = sink.getTrace(foof.getTraceId());
        assertEquals(foof, trace.getRoot());
        assertEquals(ImmutableList.<SpanData>of(subFoof), trace.getChildren(foof));
    }

    @Test
    public void testSpanDoesNotRecordAfterStartTraceIfSamplingIsOff() {
        Span.setSampler(Sampling.OFF);
        Span foof = Span.startTrace("Foof");
        Span subFoof = Span.startSpan("SubFoof");
        subFoof.addAnnotation("A");
        subFoof.end();
        foof.end();

        assertEquals(0, sink.getTraces().size());
        Trace trace = sink.getTrace(foof.getTraceId());
        assertNull(trace);
    }

    private Trace testSpan(Strategy strategy) {
        final AtomicReference<BigInteger> traceId = new AtomicReference<>();
        final AtomicReference<BigInteger> spanId = new AtomicReference<>();
        try (Span span = strategy.createSpan()) {
            traceId.set(span.getTraceId());
            spanId.set(span.getId());

            span.addAnnotation(AnnotationNames.SERVER_RECEIVED);
            span.addAnnotation(AnnotationNames.SERVICE_NAME, "testing");
            Span.startSpan("here").end();
            Span.startSpan("there").end();
            span.addAnnotation(AnnotationNames.SERVER_SENT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        assertEquals(1, sink.getTraces().size());
        assertEquals(traceId.get(), sink.getTraces().iterator().next().getId());

        Trace trace = sink.getTrace(traceId.get());

        List<SpanData> children = trace.getChildren(new SpanId(spanId.get()));
        assertEquals(2, children.size());
        assertEquals(ImmutableList.of("here", "there"), ImmutableList.copyOf(Iterables.transform(children, new Function<SpanData, String>() {
            @Override
            public String apply(SpanData input) {
                return input.getName();
            }
        })));

        assertEquals(3, trace.getAnnotations(new SpanId(spanId.get())).size());
        return trace;
    }

    private static class SpanId implements SpanData {
        private final BigInteger spanId;

        public SpanId(BigInteger spanId) {
            this.spanId = spanId;
        }

        @Override
        public BigInteger getTraceId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BigInteger getId() {
            return spanId;
        }

        @Override
        public Optional<BigInteger> getParentId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getStartTimeNanos() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getDuration() {
            throw new UnsupportedOperationException();
        }
    }

    private static interface Strategy {
        Span createSpan();
    }
}
