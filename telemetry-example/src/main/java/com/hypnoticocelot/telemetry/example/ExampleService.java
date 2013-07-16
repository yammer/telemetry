package com.hypnoticocelot.telemetry.example;

import com.google.common.collect.ImmutableList;
import com.hypnoticocelot.telemetry.sinks.TelemetryServiceSpanSink;
import com.hypnoticocelot.telemetry.tracing.InMemorySpanSinkSource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.hypnoticocelot.telemetry.example.resources.TracedResource;
import com.hypnoticocelot.telemetry.example.sink.SLF4JSpanSink;
import com.hypnoticocelot.telemetry.tracing.SpanSinkRegistry;

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
        final TelemetryServiceSpanSink serviceSink = new TelemetryServiceSpanSink();

        SpanSinkRegistry.register(loggingSink);
        SpanSinkRegistry.register(memorySinkSource);
        SpanSinkRegistry.register(serviceSink);

        final ImmutableList<Pattern> exclusionPatterns = new ImmutableList.Builder<Pattern>()
                .add(Pattern.compile("^/favicon.ico$"))
                .build();
    }

    @Override
    public void run(ExampleConfiguration configuration, Environment environment) throws Exception {
        environment.addResource(new TracedResource());
    }
}
