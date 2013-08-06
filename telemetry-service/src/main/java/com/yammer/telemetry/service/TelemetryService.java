package com.yammer.telemetry.service;

import com.yammer.dropwizard.assets.AssetsBundle;
import com.yammer.telemetry.service.resources.*;
import com.yammer.telemetry.tracing.InMemorySpanSinkSource;
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
        bootstrap.addBundle(new AssetsBundle("/META-INF/resources/webjars", "/webjars"));
    }

    @Override
    public void run(TelemetryConfiguration configuration, Environment environment) throws Exception {
        final InMemorySpanSinkSource sinkSource = new InMemorySpanSinkSource();

        environment.addResource(new SpanResource(sinkSource));
        environment.addResource(new SpansResource(sinkSource));
        environment.addResource(new TracingHomeResource(sinkSource));
        environment.addResource(new TreesResource(sinkSource));
        environment.addResource(new NetworkResource(sinkSource));
        environment.addResource(new TraceResource(sinkSource));
    }
}
