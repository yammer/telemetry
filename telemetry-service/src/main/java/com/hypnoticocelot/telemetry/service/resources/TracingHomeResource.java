package com.hypnoticocelot.telemetry.service.resources;

import com.hypnoticocelot.telemetry.service.views.TracingHomeView;
import com.hypnoticocelot.telemetry.tracing.SpanSource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/tracing")
public class TracingHomeResource {
    private final SpanSource source;

    public TracingHomeResource(SpanSource source) {
        this.source = source;
    }

    @GET
    public TracingHomeView getHomeView() {
        return new TracingHomeView(source.getTraces());
    }
}
