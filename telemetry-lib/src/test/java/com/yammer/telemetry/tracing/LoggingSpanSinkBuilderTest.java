package com.yammer.telemetry.tracing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Optional;
import org.junit.Test;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class LoggingSpanSinkBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper().setSerializationInclusion(JsonInclude.Include.NON_NULL).registerModule(new GuavaModule());

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
            public String getHost() {
                return "SomeHost";
            }

            @Override
            public String getServiceName() {
                return null;
            }

            @Override
            public String getServiceHost() {
                return null;
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

        String output = writer.toString();
        assertEquals(objectMapper.writeValueAsString(spanData) + "\n", output);
        assertFalse(output.contains("serviceName"));
        assertFalse(output.contains("serviceHost"));
    }

    @Test
    public void testLogsAnnotation() throws Exception {
        StringWriter writer = new StringWriter();
        AsynchronousSpanSink spanSink = new LoggingSpanSinkBuilder().withWriter(writer).build();

        AnnotationData annotationData = new Annotation(System.nanoTime(), "The Name", "The Message");

        spanSink.recordAnnotation(BigInteger.ONE, BigInteger.TEN, annotationData);

        assertEquals(0, spanSink.shutdown(100, TimeUnit.MILLISECONDS));

        HashMap read = objectMapper.readValue(writer.toString(), HashMap.class);

        assertEquals(10, read.get("spanId"));
        assertEquals(1, read.get("traceId"));

        List annotations = (List) read.get("annotations");
        assertEquals(1, annotations.size());

        Map annotationMap = (Map)annotations.get(0);
        assertEquals(annotationData.getStartTimeNanos(), annotationMap.get("startTimeNanos"));
        assertEquals(annotationData.getName(), annotationMap.get("name"));
        assertEquals(annotationData.getMessage(), annotationMap.get("message"));
    }

    @Test
    public void testWritingInvalidObject() throws Exception {
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

        assertTrue(writer.toString().isEmpty());
    }

    public static class Foo {
    }

}