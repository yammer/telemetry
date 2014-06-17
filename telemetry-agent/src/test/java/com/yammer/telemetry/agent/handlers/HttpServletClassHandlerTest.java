package com.yammer.telemetry.agent.handlers;

import com.yammer.telemetry.agent.test.SimpleServlet;
import com.yammer.telemetry.test.TransformedTest;
import com.yammer.telemetry.tracing.*;
import javassist.ClassPool;
import javassist.CtClass;
import org.junit.After;
import org.junit.Test;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.*;
import java.security.Principal;
import java.util.*;

import static com.yammer.telemetry.test.TelemetryTestHelpers.runTransformed;
import static org.junit.Assert.*;

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
    public void testRecordsSpans() throws Exception {
        runTransformed(TransformedTests.class, handler);
    }

    @SuppressWarnings("UnusedDeclaration")
    /**
     * This provides static methods which get invoked within the transformed classloader context.  This means we can
     * largely just write code for tests as we would.  Right now mockito doesn't play happily in this environment
     * however so instead rely on fake objects, defined below.
     */
    public static class TransformedTests {
        @TransformedTest
        public static void testBaseBehaviour() throws Exception {
            InMemorySpanSinkSource sink = new InMemorySpanSinkSource();

            Annotations.setServiceAnnotations(new ServiceAnnotations("testing"));
            SpanSinkRegistry.register(sink);

            StringWriter underlyingWriter = new StringWriter();

            HttpServletRequest request = new FakeHttpServletRequest("GET", "http://localhost:8080/foo");

            HttpServletResponse response = new FakeHttpServletResponse(underlyingWriter);

            SimpleServlet servlet = new SimpleServlet();
            servlet.service(request, response);

            assertEquals("foof", underlyingWriter.toString());

            Collection<Trace> traces = sink.getTraces();
            assertEquals(1, traces.size());
            Trace trace = traces.iterator().next();

            SpanData root = trace.getRoot();
            assertNotNull(root);

            List<AnnotationData> annotations = trace.getAnnotations(root);
            assertEquals(3, annotations.size());

            assertEquals(AnnotationNames.SERVER_RECEIVED, annotations.get(0).getName());
            assertNull(annotations.get(0).getMessage());
            assertEquals(AnnotationNames.SERVICE_NAME, annotations.get(1).getName());
            assertEquals("testing", annotations.get(1).getMessage());
            assertEquals(AnnotationNames.SERVER_SENT, annotations.get(2).getName());
            assertNull(annotations.get(2).getMessage());
        }

        @TransformedTest
        public void testRecordsRequestMethod() throws Exception {
            InMemorySpanSinkSource sink = new InMemorySpanSinkSource();

            Annotations.setServiceAnnotations(new ServiceAnnotations("testing"));
            SpanSinkRegistry.register(sink);

            FakeHttpServletRequest request = new FakeHttpServletRequest("FOOF", "http://localhost:8080/foo");

            StringWriter underlyingWriter = new StringWriter();
            HttpServletResponse response = new FakeHttpServletResponse(underlyingWriter);

            HttpServlet servlet = new HttpServlet() {
                @Override
                protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                    resp.setStatus(200);
                }
            };
            servlet.service(request, response);

            assertEquals(200, response.getStatus());

            Collection<Trace> traces = sink.getTraces();
            Trace trace = traces.iterator().next();
            SpanData rootSpan = trace.getRoot();
            assertEquals("FOOF http://localhost:8080/foo", rootSpan.getName());
        }
    }

    public static class FakeHttpServletResponse implements HttpServletResponse {
        private final PrintWriter underlying;
        private int status;

        public FakeHttpServletResponse(StringWriter underlying) {
            this.underlying = new PrintWriter(underlying);
        }

        @Override
        public void addCookie(Cookie cookie) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean containsHeader(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String encodeURL(String url) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String encodeRedirectURL(String url) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String encodeUrl(String url) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String encodeRedirectUrl(String url) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendError(int sc, String msg) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendError(int sc) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void sendRedirect(String location) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setDateHeader(String name, long date) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addDateHeader(String name, long date) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setHeader(String name, String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addHeader(String name, String value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setIntHeader(String name, int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void addIntHeader(String name, int value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setStatus(int sc) {
            this.status = sc;
        }

        @Override
        public void setStatus(int sc, String sm) {
            this.status = sc;
        }

        @Override
        public int getStatus() {
            return status;
        }

        @Override
        public String getHeader(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> getHeaders(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<String> getHeaderNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getContentType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ServletOutputStream getOutputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public PrintWriter getWriter() throws IOException {
            return underlying;
        }

        @Override
        public void setCharacterEncoding(String charset) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setContentLength(int len) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setContentType(String type) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setBufferSize(int size) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getBufferSize() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void flushBuffer() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void resetBuffer() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isCommitted() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setLocale(Locale loc) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException();
        }
    }

    public static class FakeHttpServletRequest implements HttpServletRequest {
        private final String method;
        private final StringBuffer requestURL;

        public FakeHttpServletRequest(String method, String requestURL) {
            this.method = method;
            this.requestURL = new StringBuffer(requestURL);
        }

        @Override
        public String getAuthType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Cookie[] getCookies() {
            throw new UnsupportedOperationException();
        }

        @Override
        public long getDateHeader(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getHeader(String name) {
            return null;
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getIntHeader(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getMethod() {
            return method;
        }

        @Override
        public String getPathInfo() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getPathTranslated() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getContextPath() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getQueryString() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRemoteUser() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isUserInRole(String role) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRequestedSessionId() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRequestURI() {
            throw new UnsupportedOperationException();
        }

        @Override
        public StringBuffer getRequestURL() {
            return requestURL;
        }

        @Override
        public String getServletPath() {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpSession getSession(boolean create) {
            throw new UnsupportedOperationException();
        }

        @Override
        public HttpSession getSession() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean authenticate(HttpServletResponse response) throws IOException, ServletException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void login(String username, String password) throws ServletException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void logout() throws ServletException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Collection<Part> getParts() throws IOException, ServletException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Part getPart(String name) throws IOException, ServletException {
            throw new UnsupportedOperationException();
        }

        @Override
        public Object getAttribute(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setCharacterEncoding(String env) throws UnsupportedEncodingException {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getContentLength() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getContentType() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getParameter(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Enumeration<String> getParameterNames() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String[] getParameterValues(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getProtocol() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getScheme() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getServerName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getServerPort() {
            throw new UnsupportedOperationException();
        }

        @Override
        public BufferedReader getReader() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRemoteAddr() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRemoteHost() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setAttribute(String name, Object o) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void removeAttribute(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Enumeration<Locale> getLocales() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isSecure() {
            throw new UnsupportedOperationException();
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getRealPath(String path) {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getRemotePort() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getLocalName() {
            throw new UnsupportedOperationException();
        }

        @Override
        public String getLocalAddr() {
            throw new UnsupportedOperationException();
        }

        @Override
        public int getLocalPort() {
            throw new UnsupportedOperationException();
        }

        @Override
        public ServletContext getServletContext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            throw new UnsupportedOperationException();
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAsyncStarted() {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean isAsyncSupported() {
            throw new UnsupportedOperationException();
        }

        @Override
        public AsyncContext getAsyncContext() {
            throw new UnsupportedOperationException();
        }

        @Override
        public DispatcherType getDispatcherType() {
            throw new UnsupportedOperationException();
        }
    }

}
