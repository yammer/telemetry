package com.yammer.telemetry.tracing;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import org.junit.Test;

import java.io.StringWriter;
import java.math.BigInteger;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class LoggingSpanSinkBuilderTest {

    private final ObjectMapper objectMapper = new ObjectMapper().setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy()).setSerializationInclusion(JsonInclude.Include.NON_NULL).registerModule(new GuavaModule());

    @Test
    public void testLogsSpanData() throws Exception {
        StringWriter writer = new StringWriter();
        AsynchronousSpanSink spanSink = new LoggingSpanSinkBuilder().withWriter(writer).build();

        final long theStartTime = System.nanoTime();
        SpanData spanData = new SpanData() {
            private long startTime = theStartTime;

            @Override
            public BigInteger getTraceId() {
                return BigInteger.ONE;
            }

            @Override
            public BigInteger getSpanId() {
                return BigInteger.TEN;
            }

            @Override
            public Optional<BigInteger> getParentSpanId() {
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
            public long getStartTime() {
                return startTime;
            }

            @Override
            public long getDuration() {
                return 100;
            }

            @Override
            public List<AnnotationData> getAnnotations() {
                return ImmutableList.<AnnotationData>of(new AnnotationData() {
                    @Override
                    public long getLoggedAt() {
                        return theStartTime + 10;
                    }

                    @Override
                    public String getName() {
                        return "Annotation";
                    }

                    @Override
                    public String getMessage() {
                        return "Annotation Message";
                    }
                });
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
    public void testWritingInvalidObject() throws Exception {
        StringWriter writer = new StringWriter();
        AsynchronousSpanSink spanSink = new LoggingSpanSinkBuilder().withWriter(writer).build();

        @SuppressWarnings("UnusedDeclaration") SpanData spanData = new SpanData() {
            private long startTime = System.nanoTime();

            @Override
            public BigInteger getTraceId() {
                return BigInteger.ONE;
            }

            @Override
            public BigInteger getSpanId() {
                return BigInteger.TEN;
            }

            @Override
            public Optional<BigInteger> getParentSpanId() {
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
            public long getStartTime() {
                return startTime;
            }

            @Override
            public long getDuration() {
                return 100;
            }

            @Override
            public List<AnnotationData> getAnnotations() {
                return ImmutableList.of();
            }

            public Foo getFoo() {
                return new Foo();
            }
        };

        spanSink.record(spanData);

        assertEquals(0, spanSink.shutdown(100, TimeUnit.MILLISECONDS));

        assertTrue(writer.toString().isEmpty());
    }

    public static class Foo {
    }

}