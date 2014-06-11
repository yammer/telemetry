package com.yammer.telemetry.service.resources;

import com.yammer.telemetry.service.views.TraceView;
import com.yammer.telemetry.tracing.Trace;
import com.yammer.telemetry.tracing.SpanSource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import java.math.BigInteger;

@Path("/tracing/{id}")
public class TraceResource {
    private final SpanSource source;

    public TraceResource(SpanSource source) {
        this.source = source;
    }

    @GET
    public TraceView getTraceView(@PathParam("id") BigInteger traceId) {
        final Trace trace = source.getTrace(traceId);
        if (trace != null) {
            return new TraceView(trace);
        } else {
            throw new WebApplicationException(Response.Status.NOT_FOUND);
        }
    }
}
