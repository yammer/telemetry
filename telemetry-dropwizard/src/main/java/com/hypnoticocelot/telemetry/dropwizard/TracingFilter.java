package com.hypnoticocelot.telemetry.dropwizard;

import com.google.common.collect.ImmutableMap;
import com.hypnoticocelot.telemetry.SpanData;
import com.hypnoticocelot.telemetry.tracing.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.regex.Pattern;

public class TracingFilter implements Filter {
    private static final Logger LOG = LoggerFactory.getLogger(TracingFilter.class);

    private final List<Pattern> exclusionPatterns;
    private String localHost = "unknown";

    public TracingFilter(List<Pattern> exclusionPatterns) {
        this.exclusionPatterns = exclusionPatterns;

        try {
            this.localHost = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            LOG.warn("Unable to determine IP address of localhost, all spans will be labeled 'unknown'", e);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        final HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        for (Pattern pattern : exclusionPatterns) {
            if (pattern.matcher(httpServletRequest.getRequestURI()).matches()) {
                chain.doFilter(request, response);
                return;
            }
        }

        final ImmutableMap.Builder<String, String> annotationBuilder = new ImmutableMap.Builder<>();
        annotationBuilder.put("localHost", localHost);

        String spanName = "Generic HTTP Request";
        if (request instanceof HttpServletRequest) {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            spanName = "HTTP " + httpRequest.getMethod() + " "
                    + httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + httpRequest.getRequestURI();
            annotationBuilder.put("httpMethod", httpRequest.getMethod());
            annotationBuilder.put("httpUri", httpRequest.getRequestURI());
        }

        try (Span span = Span.start(new SpanData(spanName, annotationBuilder.build()))) {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
