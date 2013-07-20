package com.hypnoticocelot.telemetry.example;

import com.hypnoticocelot.telemetry.example.resources.ProxyResource;
import com.hypnoticocelot.telemetry.sinks.TelemetryServiceSpanSink;
import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.hypnoticocelot.telemetry.example.resources.TracedResource;
import com.hypnoticocelot.telemetry.example.sink.SLF4JSpanSink;
import com.hypnoticocelot.telemetry.tracing.SpanSinkRegistry;

public class ExampleService extends Service<ExampleConfiguration> {
    public static void main(String[] args) throws Exception {
        new ExampleService().run(args);
    }

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.setName("example");

        final SLF4JSpanSink loggingSink = new SLF4JSpanSink();
        final TelemetryServiceSpanSink serviceSink = new TelemetryServiceSpanSink();

        SpanSinkRegistry.register(loggingSink);
        SpanSinkRegistry.register(serviceSink);
    }

    @Override
    public void run(ExampleConfiguration configuration, Environment environment) throws Exception {
        Client client = new JerseyClientBuilder().using(environment).build();

        environment.addResource(new ProxyResource(client));
        environment.addResource(new TracedResource());
    }
}
