package com.hypnoticocelot.telemetry.dropwizard;

import com.hypnoticocelot.telemetry.tracing.Span;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

public class TracingFilter implements Filter {
    private final List<Pattern> exclusionPatterns;

    public TracingFilter(List<Pattern> exclusionPatterns) {
        this.exclusionPatterns = exclusionPatterns;
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

        String spanName = "Generic HTTP Request";
        if (request instanceof HttpServletRequest) {
            final HttpServletRequest httpRequest = (HttpServletRequest) request;
            spanName = "HTTP " + httpRequest.getMethod() + " "
                    + httpRequest.getScheme() + "://" + httpRequest.getHeader("Host") + httpRequest.getRequestURI();
        }

        try (Span span = Span.start(spanName)) {
            chain.doFilter(request, response);
        }
    }

    @Override
    public void destroy() {

    }
}
