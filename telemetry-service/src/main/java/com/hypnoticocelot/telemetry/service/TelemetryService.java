package com.hypnoticocelot.telemetry.service;

import com.hypnoticocelot.telemetry.service.resources.SpansResource;
import com.hypnoticocelot.telemetry.tracing.InMemorySpanSinkSource;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class TelemetryService extends Service<TelemetryConfiguration> {
    public static void main(String[] args) throws Exception {
        new TelemetryService().run(args);
    }

    @Override
    public void initialize(Bootstrap<TelemetryConfiguration> bootstrap) {

    }

    @Override
    public void run(TelemetryConfiguration configuration, Environment environment) throws Exception {
        final InMemorySpanSinkSource sinkSource = new InMemorySpanSinkSource();

        environment.addResource(new SpansResource(sinkSource));
    }
}
