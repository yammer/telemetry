package com.yammer.telemetry.agent.handlers;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.yammer.telemetry.agent.Annotations;
import com.yammer.telemetry.agent.ServiceAnnotations;
import com.yammer.telemetry.agent.TelemetryTransformer;
import com.yammer.telemetry.agent.test.SimpleServlet;
import com.yammer.telemetry.agent.test.TransformingClassLoader;
import com.yammer.telemetry.tracing.*;
import javassist.ClassPool;
import javassist.CtClass;
import org.junit.After;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;

import javax.annotation.Nullable;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class HttpServletClassHandlerTest {
    private HttpServletClassHandler handler = new HttpServletClassHandler();

    @After
    public void clearSpanSinkRegistry() {
        SpanSinkRegistry.clear();
    }

    @Test
    public void testNothingForNonHttpServletClasses() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get("java.lang.String");
        assertFalse(handler.transformed(ctClass, cp));
    }

    @Test
    public void testTransformsHttpServletClasses() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get("javax.servlet.http.HttpServlet");
        assertTrue(handler.transformed(ctClass, cp));
    }

    @Test
    public void testTransformsHttpServletSubclassesThatOverrideService() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get("com.sun.jersey.spi.container.servlet.ServletContainer");
        assertTrue(handler.transformed(ctClass, cp));
    }

    @Test
    public void testNothingForHttpServletSubclassesWithoutServiceMethodOverride() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get("com.yammer.dropwizard.tasks.TaskServlet");
        assertFalse(handler.transformed(ctClass, cp));
    }

    @Test
    public void testWrapsMethodToRecordSpan() throws Exception{
        Annotations.setServiceAnnotations(new ServiceAnnotations("test HttpServlet"));

        InMemorySpanSinkSource sink = new InMemorySpanSinkSource();
        SpanSinkRegistry.register(sink);

        TelemetryTransformer transformer = new TelemetryTransformer();
        transformer.addHandler(handler);

        StringWriter underlyingWriter = new StringWriter();

        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURL()).thenReturn(new StringBuffer("http://localhost:8080/foo"));

        HttpServletResponse response = mock(HttpServletResponse.class);
        when(response.getWriter()).thenReturn(new PrintWriter(underlyingWriter));

        try (TransformingClassLoader loader = new TransformingClassLoader(SimpleServlet.class, transformer)) {

            Class<?> servletClass = loader.loadClass("com.yammer.telemetry.agent.test.SimpleServlet");
            Method serviceMethod = servletClass.getMethod("service", ServletRequest.class, ServletResponse.class);

            Object instance = servletClass.newInstance();

            assertTrue(sink.getTraces().isEmpty());

            serviceMethod.invoke(instance, request, response);

            assertEquals("foof", underlyingWriter.toString());

            assertFalse(sink.getTraces().isEmpty());
            assertEquals(1, sink.getTraces().size());
            Trace trace = sink.getTraces().iterator().next();

            assertEquals(trace.getId(), trace.getRoot().getTraceId());
            assertEquals("http://localhost:8080/foo", trace.getRoot().getName());
        }
    }

}
