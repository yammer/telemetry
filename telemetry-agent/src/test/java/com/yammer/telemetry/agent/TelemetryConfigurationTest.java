package com.yammer.telemetry.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.config.ConfigurationException;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.telemetry.tracing.Sampling;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import static org.junit.Assert.*;

public class TelemetryConfigurationTest {
    @Test
    public void testConstructionDefaults() {
        TelemetryConfiguration configuration = new TelemetryConfiguration();

        assertEquals(ImmutableList.<String>of(), configuration.getInstruments());
        assertEquals("unknown", configuration.getAnnotations().getService());
        assertEquals(Sampling.ON, configuration.getSampler());

        SinkConfiguration sinks = configuration.getSinks();
        assertFalse(sinks.isEnabled());

        assertFalse(sinks.getLog().isEnabled());
        assertNull(sinks.getLog().getFile());

        assertFalse(sinks.getTelemetry().isEnabled());
        assertNull(sinks.getTelemetry().getHost());
        assertNull(sinks.getTelemetry().getPort());
    }

    @Test
    public void testLoadingSampleConfiguration() throws IOException, ConfigurationException {
        URL url = this.getClass().getResource("/telemetry.yml");
        File file = new File(url.getFile());

        TelemetryConfiguration configuration = loadConfiguration(file);

        assertEquals(ImmutableList.of("inbound-http", "outbound-http", "database"), configuration.getInstruments());
        assertEquals("example", configuration.getAnnotations().getService());
        assertEquals(Sampling.OFF, configuration.getSampler());

        SinkConfiguration sinks = configuration.getSinks();
        assertTrue(sinks.isEnabled());

        assertTrue(sinks.getLog().isEnabled());
        assertEquals("example-telemetry.log", sinks.getLog().getFile());

        assertTrue(sinks.getTelemetry().isEnabled());
        assertEquals("localhost", sinks.getTelemetry().getHost());
        assertEquals(Integer.valueOf(9090), sinks.getTelemetry().getPort());
    }

    private TelemetryConfiguration loadConfiguration(File file) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        TelemetryConfiguration configuration = mapper.readValue(file, TelemetryConfiguration.class);
        ImmutableList<String> errors = new Validator().validate(configuration);
        assertTrue(errors.asList().toString(), errors.isEmpty());
        return configuration;
    }

}