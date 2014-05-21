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
import com.yammer.telemetry.tracing.SpanSink;
import com.yammer.telemetry.tracing.SpanSinkRegistry;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.net.URI;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

public class TelemetryAgentTest {
    private Instrumentation instrumentation = mock(Instrumentation.class);

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

        TelemetryAgent.agentmain("src/test/resources/telemetry.yml", instrumentation);

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
        File f = new File("src/test/resources/telemetry.yml");
        System.out.println("><><>< " + f.getAbsolutePath() + " ><><><><");

        TelemetryAgent.agentmain("src/test/resources/telemetry.yml", instrumentation);

        assertEquals("example", Annotations.getServiceAnnotations().getService());
    }

    @Test
    public void validArgsRegistersSinks() throws Exception {
        TelemetryAgent.agentmain("src/test/resources/telemetry.yml", instrumentation);

        ImmutableList<SpanSink> registeredSinks = ImmutableList.copyOf(SpanSinkRegistry.getSpanSinks());
        assertEquals(1, registeredSinks.size());

        TelemetryServiceSpanSink telemetrySink = (TelemetryServiceSpanSink) registeredSinks.get(0);
        assertEquals(URI.create("http://localhost:9090/spans"), telemetrySink.getBaseUri());
    }
}
