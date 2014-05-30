package com.yammer.telemetry.tracing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class LoggingSpanSinkBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new GuavaModule());

    @Test
    public void testLogsSpanData() throws Exception {
        StringWriter writer = new StringWriter();
        AsynchronousSpanSink spanSink = new LoggingSpanSinkBuilder().withWriter(writer).build();

        SpanData spanData = new SpanData() {
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
        };

        spanSink.record(spanData);

        assertEquals(0, spanSink.shutdown(100, TimeUnit.MILLISECONDS));

        assertEquals(objectMapper.writeValueAsString(spanData) + "\n", writer.toString());
    }

    @Test
    public void testLogsAnnotation() throws Exception {
        StringWriter writer = new StringWriter();
        AsynchronousSpanSink spanSink = new LoggingSpanSinkBuilder().withWriter(writer).build();

        AnnotationData annotationData = new AnnotationData() {
            private long startTime = System.nanoTime();

            @Override
            public long getStartTimeNanos() {
                return startTime;
            }

            @Override
            public String getName() {
                return "The Name";
            }

            @Override
            public String getMessage() {
                return "The Message";
            }
        };

        spanSink.recordAnnotation(BigInteger.ONE, BigInteger.TEN, annotationData);

        assertEquals(0, spanSink.shutdown(100, TimeUnit.MILLISECONDS));

        Map wrappedAnnotationData = ImmutableMap.of(
                "spanId", BigInteger.TEN.toString(),
                "traceId", BigInteger.ONE.toString(),
                "annotations", ImmutableList.of(annotationData)
        );
        assertEquals(objectMapper.writeValueAsString(wrappedAnnotationData) + "\n", writer.toString());
    }

    @Test
    public void testWritingInvalidObject() throws Exception {
//        String s = objectMapper.writeValueAsString(new Foo());
//        System.out.println("s = " + s);

        StringWriter writer = new StringWriter();
        AsynchronousSpanSink spanSink = new LoggingSpanSinkBuilder().withWriter(writer).build();

        AnnotationData annotationData = new AnnotationData() {
            private long startTime = System.nanoTime();
            private Foo otherThing = new Foo();

            @Override
            public long getStartTimeNanos() {
                return startTime;
            }

            @Override
            public String getName() {
                return "The Name";
            }

            @Override
            public String getMessage() {
                return "The Message";
            }

            public Foo getFoo() {
                return otherThing;
            }
        };

        spanSink.recordAnnotation(BigInteger.ONE, BigInteger.TEN, annotationData);

        assertEquals(0, spanSink.shutdown(100, TimeUnit.MILLISECONDS));

        Map wrappedAnnotationData = ImmutableMap.of(
                "spanId", BigInteger.TEN.toString(),
                "traceId", BigInteger.ONE.toString(),
                "annotations", ImmutableList.of(annotationData)
        );

        assertTrue(writer.toString().isEmpty());
    }

    public static class Foo {
//        private Foo foo = this;
    }

}