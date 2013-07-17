package com.hypnoticocelot.telemetry.service;

import com.hypnoticocelot.telemetry.service.resources.SpansResource;
import com.hypnoticocelot.telemetry.service.resources.TraceResource;
import com.hypnoticocelot.telemetry.service.resources.TracingHomeResource;
import com.hypnoticocelot.telemetry.service.resources.TreesResource;
import com.hypnoticocelot.telemetry.tracing.InMemorySpanSinkSource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.views.ViewBundle;

public class TelemetryService extends Service<TelemetryConfiguration> {
    public static void main(String[] args) throws Exception {
        new TelemetryService().run(args);
    }

    @Override
    public void initialize(Bootstrap<TelemetryConfiguration> bootstrap) {
        bootstrap.addBundle(new ViewBundle());
    }

    @Override
    public void run(TelemetryConfiguration configuration, Environment environment) throws Exception {
        final InMemorySpanSinkSource sinkSource = new InMemorySpanSinkSource();

        final TracingHomeResource tracingHomeResource = new TracingHomeResource(sinkSource);
        final TreesResource treesResource = new TreesResource(sinkSource);
        final TraceResource traceResource = new TraceResource(sinkSource);

        environment.addResource(new SpansResource(sinkSource));
        environment.addResource(tracingHomeResource);
        environment.addResource(treesResource);
        environment.addResource(traceResource);
    }
}
