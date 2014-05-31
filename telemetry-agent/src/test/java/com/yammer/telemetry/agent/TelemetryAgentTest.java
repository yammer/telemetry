package com.yammer.telemetry.agent;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.yammer.telemetry.agent.handlers.ApacheHttpClientClassHandler;
import com.yammer.telemetry.agent.handlers.ClassInstrumentationHandler;
import com.yammer.telemetry.agent.handlers.HttpServletClassHandler;
import com.yammer.telemetry.agent.jdbc.JdbcDriverClassHandler;
import com.yammer.telemetry.sinks.TelemetryServiceSpanSink;
import com.yammer.telemetry.tracing.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.File;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TelemetryAgentTest {
    private Instrumentation instrumentation = mock(Instrumentation.class);
    private Sampling defaultSampler;

    @Before
    public void captureDefaultSampling() {
        defaultSampler = Span.getSampler();
    }

    @After
    public void resetDefaultSampling() {
        Span.setSampler(defaultSampler);
    }

    @Test(expected = NullPointerException.class)
    public void nullArgs() {
        TelemetryAgent.agentmain(null, instrumentation);
        verifyZeroInteractions(instrumentation);
    }

    @Test
    public void emptyArgs() throws Exception {
        TelemetryAgent.agentmain("", instrumentation);
        verifyZeroInteractions(instrumentation);
    }

    @Test
    public void invalidArgs() throws Exception {
        TelemetryAgent.agentmain("woo", instrumentation);
        verifyZeroInteractions(instrumentation);
    }

    @Test
    public void validArgsAddsTransformers() throws Exception {
        ArgumentCaptor<ClassFileTransformer> captor = ArgumentCaptor.forClass(ClassFileTransformer.class);
        doNothing().when(instrumentation).addTransformer(captor.capture());

        TelemetryAgent.agentmain(getConfigurationPath(), instrumentation);

        List<ClassFileTransformer> allValues = captor.getAllValues();
        assertNotNull(allValues);
        assertEquals(1, allValues.size());

        TelemetryTransformer transformer = (TelemetryTransformer) allValues.get(0);
        Set<Class<?>> transformerClasses = ImmutableSet.copyOf(Iterables.transform(transformer.getHandlers(), new Function<ClassInstrumentationHandler, Class<?>>() {
            @Override
            public Class<?> apply(ClassInstrumentationHandler input) {
                return input.getClass();
            }
        }));

        assertEquals(ImmutableSet.<Class<?>>of(ApacheHttpClientClassHandler.class, HttpServletClassHandler.class, JdbcDriverClassHandler.class), transformerClasses);
    }

    @Test
    public void validArgsAddsServiceAnnotations() throws Exception {
        TelemetryAgent.agentmain(getConfigurationPath(), instrumentation);

        assertEquals("example", Annotations.getServiceAnnotations().getService());
    }

    @Test
    public void validArgsRegistersSinks() throws Exception {
        TelemetryAgent.agentmain(getConfigurationPath(), instrumentation);

        ImmutableList<SpanSink> registeredSinks = ImmutableList.copyOf(SpanSinkRegistry.getSpanSinks());
        assertEquals(2, registeredSinks.size());

        TelemetryServiceSpanSink telemetrySink = (TelemetryServiceSpanSink) registeredSinks.get(0);
        assertEquals(URI.create("http://localhost:9090/spans"), telemetrySink.getBaseUri());

        // todo - improve this test
        assertTrue(registeredSinks.get(1) instanceof AsynchronousSpanSink);
    }

    @Test
    public void configuresSpanSampleLevels() throws Exception {
        assertEquals(Sampling.ON, Span.getSampler());

        TelemetryAgent.agentmain(getConfigurationPath(), instrumentation);

        assertEquals(Sampling.OFF, Span.getSampler());
    }

    private String getConfigurationPath() {
        URL url = this.getClass().getResource("/telemetry.yml");
        File file = new File(url.getFile());
        return file.getAbsolutePath();
    }
}
