package com.yammer.telemetry.tracing.logging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import com.yammer.telemetry.tracing.AnnotationData;
import com.yammer.telemetry.tracing.BeanSpanData;
import org.junit.Test;

import java.io.IOException;
import java.io.StringWriter;
import java.math.BigInteger;

import static org.junit.Assert.assertEquals;

public class LogJobFactoryTest {
    @Test(expected = IOException.class)
    public void testCreateWithFileFailsIfNotWriteable() throws Exception {
        LogJobFactory.withFile("/");
    }

    @Test
    public void testCreateWithFileSucceedsIfWriteable() throws Exception {
        LogJobFactory.withFile("sample.log");
    }

    @Test(expected = NullPointerException.class)
    public void testCreateWithNullFile() throws Exception {
        LogJobFactory.withFile(null);
    }

    @Test
    public void testCreateWithWriter() throws Exception {
        LogJobFactory.withWriter(new StringWriter());
    }

    @Test(expected = NullPointerException.class)
    public void testCreateWithNullWriter() throws Exception {
        LogJobFactory.withWriter(null);
    }

    @Test
    public void testWriteNull() throws Exception {
        StringWriter writer = new StringWriter();
        LogJobFactory logJobFactory = LogJobFactory.withWriter(writer);

        Runnable job = logJobFactory.createJob(null);
        job.run();

        assertEquals(String.format("null%n"), writer.toString());
    }

    @Test
    public void testWriteSpanData() throws Exception {
        StringWriter writer = new StringWriter();
        LogJobFactory logJobFactory = LogJobFactory.withWriter(writer);

        BeanSpanData expectedData = new BeanSpanData(100, "host", "name", Optional.<BigInteger>absent(), BigInteger.ZERO, 15, BigInteger.ONE, ImmutableList.<AnnotationData>of());
        Runnable job = logJobFactory.createJob(expectedData);
        job.run();

        ObjectMapper objectMapper = new ObjectMapper().setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy()).setSerializationInclusion(JsonInclude.Include.NON_NULL).registerModule(new GuavaModule());
        BeanSpanData actualData = objectMapper.readValue(writer.toString(), BeanSpanData.class);

        assertEquals(expectedData, actualData);
    }
}