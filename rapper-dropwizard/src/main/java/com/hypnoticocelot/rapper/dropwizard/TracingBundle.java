package com.hypnoticocelot.rapper.dropwizard;

import com.google.common.collect.ImmutableList;
import com.hypnoticocelot.rapper.dropwizard.resources.TraceResource;
import com.hypnoticocelot.rapper.dropwizard.resources.TreesResource;
import com.sun.jersey.api.uri.UriTemplateParser;
import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;
import com.hypnoticocelot.rapper.dropwizard.resources.TracingHomeResource;
import com.hypnoticocelot.rapper.tracing.SpanSource;

import javax.ws.rs.Path;
import java.util.List;
import java.util.regex.Pattern;

public class TracingBundle implements Bundle {
    private final SpanSource spanSource;
    private final ImmutableList<Pattern> exclusionPatterns;

    public TracingBundle(SpanSource spanSource, ImmutableList<Pattern> exclusionPatterns) {
        this.spanSource = spanSource;
        this.exclusionPatterns = exclusionPatterns;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
    }

    @Override
    public void run(Environment environment) {
        final TracingHomeResource tracingHomeResource = new TracingHomeResource(spanSource);
        final TreesResource treesResource = new TreesResource(spanSource);
        final TraceResource traceResource = new TraceResource(spanSource);

        ImmutableList<Pattern> patterns = new ImmutableList.Builder<Pattern>().addAll(exclusionPatterns)
                .addAll(patternsFor(tracingHomeResource, treesResource, traceResource)).build();

        environment.addFilter(new TracingFilter(patterns), "/*");

        environment.addResource(tracingHomeResource);
        environment.addResource(treesResource);
        environment.addResource(traceResource);
    }

    private List<Pattern> patternsFor(Object... resources) {
        ImmutableList.Builder<Pattern> patternBuilder = new ImmutableList.Builder<>();

        for (Object resource : resources) {
            Path annotation = resource.getClass().getAnnotation(Path.class);
            if (annotation != null) {
                patternBuilder.add(new UriTemplateParser(annotation.value()).getPattern());
            }
        }

        return patternBuilder.build();
    }
}
