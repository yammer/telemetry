package com.hypnoticocelot.rapper.dropwizard.resources;

import com.hypnoticocelot.rapper.dropwizard.views.TracingHomeView;
import com.hypnoticocelot.rapper.tracing.SpanSource;

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
