package com.yammer.telemetry.agent.handlers;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.yammer.telemetry.test.TransformedTest;
import com.yammer.telemetry.tracing.*;
import javassist.ClassPool;
import javassist.CtClass;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.conn.BasicClientConnectionManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

import java.io.IOException;
import java.net.URL;
import java.util.Collection;
import java.util.List;

import static com.yammer.telemetry.test.TelemetryTestHelpers.runTransformed;
import static org.junit.Assert.*;

public class ApacheHttpClientClassHandlerTest {
    private ApacheHttpClientClassHandler handler = new ApacheHttpClientClassHandler();

    @Test
    public void testHttpClientDoesNotGetTransformedBecauseItIsAbstract() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get(HttpClient.class.getName());
        assertFalse(handler.transform(ctClass, cp));
    }

    @Test
    public void testHttpClientSubClassDoesGetTransformedIfNonAbstractExecuteMethodExists() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get(HttpClientSubClass.class.getName());
        assertTrue(handler.transform(ctClass, cp));
    }

    @Test
    public void testHttpClientAbstractSubClassDoesNotGetTransformed() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get(HttpClientAbstractSubClass.class.getName());
        assertFalse(handler.transform(ctClass, cp));
    }

    @Test
    public void testHttpClientSubClassGetsTransformed() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get(AbstractHttpClient.class.getName());
        assertTrue(handler.transform(ctClass, cp));
    }

    @Test
    public void testNonHttpClientClassDoesNotGetTransformed() throws Exception {
        ClassPool cp = ClassPool.getDefault();
        CtClass ctClass = cp.get(String.class.getName());
        assertFalse(handler.transform(ctClass, cp));
    }

    @Test
    public void runTransformedTests() throws Exception {
        runTransformed(TransformedTests.class, handler);
    }

    @Test
    public void testUsingHttpClientRecordsSpan() throws Exception {
        BasicHttpResponse expectedResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");

        HttpClient client = new TestableHttpClient(expectedResponse);

        URL localResourceUrl = TransformedTest.class.getResource("/telemetry.yml");

        HttpResponse httpResponse = client.execute(new HttpGet(localResourceUrl.toURI()));

        assertEquals(expectedResponse, httpResponse);
    }

    @SuppressWarnings("UnusedDeclaration")
    public static class TransformedTests {
        @TransformedTest
        public static void testUsingHttpClientRecordsSpan() throws Exception {
            InMemorySpanSinkSource sink = new InMemorySpanSinkSource();
            SpanSinkRegistry.register(sink);

            HttpGet request = new HttpGet("http://anything");
            try (Span trace = Span.startTrace("Test")) {

                BasicHttpResponse expectedResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
                HttpClient client = new TestableHttpClient(expectedResponse);

                HttpResponse httpResponse = client.execute(request);

                assertEquals(expectedResponse, httpResponse);

            }

            Collection<Trace> traces = sink.getTraces();
            assertEquals(1, traces.size());

            Trace trace = traces.iterator().next();
            SpanData root = trace.getRoot();
            assertEquals("Test", root.getName());

            List<SpanData> spans = trace.getChildren(root);
            assertEquals(1, spans.size());

            SpanData httpClientSpan = spans.get(0);
            assertEquals("http://anything", httpClientSpan.getName());

            Multimap<String, String> annotationsMap = LinkedListMultimap.create();
            for (AnnotationData annotation : trace.getAnnotations(httpClientSpan)) {
                annotationsMap.put(annotation.getName(), annotation.getMessage());
            }

            assertTrue(annotationsMap.containsKey(AnnotationNames.CLIENT_SENT));
            assertTrue(annotationsMap.containsKey(AnnotationNames.CLIENT_RECEIVED));
            assertFalse(annotationsMap.containsKey(AnnotationNames.CLIENT_EXCEPTION));
        }

        @TransformedTest
        public static void testUsingHttpClientPropagatesTraceAndSpanIds() throws Exception {
            InMemorySpanSinkSource sink = new InMemorySpanSinkSource();
            SpanSinkRegistry.register(sink);

            HttpGet request = new HttpGet("http://anything");
            try (Span trace = Span.startTrace("Test")) {

                BasicHttpResponse expectedResponse = new BasicHttpResponse(HttpVersion.HTTP_1_1, 200, "OK");
                HttpClient client = new TestableHttpClient(expectedResponse);

                assertEquals(expectedResponse, client.execute(request));
            }

            Trace trace = sink.getTraces().iterator().next();
            SpanData httpClientSpan = trace.getChildren(trace.getRoot()).get(0);

            Header traceHeader = request.getFirstHeader(HttpHeaderNames.TRACE_ID);
            Header spanHeader = request.getFirstHeader(HttpHeaderNames.SPAN_ID);

            assertNotNull(traceHeader);
            assertNotNull(spanHeader);
            assertEquals(trace.getTraceId().toString(), traceHeader.getValue());
            assertEquals(httpClientSpan.getSpanId().toString(), spanHeader.getValue());
        }
    }

    public static class TestableHttpClient implements HttpClient {
        private final HttpResponse result;

        public TestableHttpClient(HttpResponse result) {
            this.result = result;
        }

        @Override
        public HttpParams getParams() {
            return new BasicHttpParams();
        }

        @Override
        public ClientConnectionManager getConnectionManager() {
            return new BasicClientConnectionManager();
        }

        @Override
        public HttpResponse execute(HttpUriRequest request) {
            return result;
        }

        @Override
        public HttpResponse execute(HttpUriRequest request, HttpContext context) {
            return result;
        }

        @Override
        public HttpResponse execute(HttpHost target, HttpRequest request) {
            return result;
        }

        @Override
        public HttpResponse execute(HttpHost target, HttpRequest request, HttpContext context) {
            return result;
        }

        @Override
        public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
            return responseHandler.handleResponse(result);
        }

        @Override
        public <T> T execute(HttpUriRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
            return responseHandler.handleResponse(result);
        }

        @Override
        public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler) throws IOException {
            return responseHandler.handleResponse(result);
        }

        @Override
        public <T> T execute(HttpHost target, HttpRequest request, ResponseHandler<? extends T> responseHandler, HttpContext context) throws IOException {
            return responseHandler.handleResponse(result);
        }
    }

    public static abstract class HttpClientSubClass implements HttpClient {
        @Override
        public HttpResponse execute(HttpHost target, HttpRequest request) {
            return null;
        }
    }

    public static abstract class HttpClientAbstractSubClass implements HttpClient {
        @Override
        public abstract HttpResponse execute(HttpHost target, HttpRequest request);
    }
}