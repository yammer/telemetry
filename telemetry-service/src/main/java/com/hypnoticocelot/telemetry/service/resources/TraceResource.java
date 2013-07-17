package com.hypnoticocelot.telemetry.service.resources;

import com.hypnoticocelot.telemetry.service.views.TraceView;
import com.hypnoticocelot.telemetry.tracing.Trace;
import com.hypnoticocelot.telemetry.tracing.SpanSource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.util.UUID;

@Path("/tracing/{id}")
public class TraceResource {
    private final SpanSource source;

    public TraceResource(SpanSource source) {
        this.source = source;
    }

    @GET
    public TraceView getTraceView(@PathParam("id") String traceId) {
        final Trace trace = source.getTrace(UUID.fromString(traceId));
        if (trace != null) {
            return new TraceView(trace);
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }
}
