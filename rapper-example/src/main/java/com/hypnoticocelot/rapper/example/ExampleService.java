package com.hypnoticocelot.rapper.example;

import com.google.common.collect.ImmutableList;
import com.hypnoticocelot.rapper.tracing.InMemorySpanSinkSource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.hypnoticocelot.rapper.dropwizard.TracingBundle;
import com.hypnoticocelot.rapper.example.resources.TracedResource;
import com.hypnoticocelot.rapper.example.sink.SLF4JSpanSink;
import com.hypnoticocelot.rapper.tracing.SpanSinkRegistry;

import java.util.regex.Pattern;

public class ExampleService extends Service<ExampleConfiguration> {
    public static void main(String[] args) throws Exception {
        new ExampleService().run(args);
    }

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.setName("example");

        final SLF4JSpanSink loggingSink = new SLF4JSpanSink();
        final InMemorySpanSinkSource memorySinkSource = new InMemorySpanSinkSource();

        SpanSinkRegistry.register(loggingSink);
        SpanSinkRegistry.register(memorySinkSource);

        final ImmutableList<Pattern> exclusionPatterns = new ImmutableList.Builder<Pattern>()
                .add(Pattern.compile("^/favicon.ico$"))
                .build();

        bootstrap.addBundle(new TracingBundle(memorySinkSource, exclusionPatterns));
    }

    @Override
    public void run(ExampleConfiguration configuration, Environment environment) throws Exception {
        environment.addResource(new TracedResource());
    }
}
