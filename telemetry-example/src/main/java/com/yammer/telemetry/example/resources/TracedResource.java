package com.yammer.telemetry.example.resources;

import com.yammer.telemetry.tracing.SpanInfo;
import com.yammer.telemetry.tracing.Span;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/traced")
@Produces(MediaType.TEXT_PLAIN)
public class TracedResource {
    @GET
    public String traceThis() throws InterruptedException {
        try (Span span = Span.start(new SpanInfo("sleep 1"))) {
            Thread.sleep(100);
        }

        try (Span span = Span.start(new SpanInfo("sleep 2"))) {
            Thread.sleep(100);
        }

        try (Span span = Span.start(new SpanInfo("sleep 3"))) {
            Thread.sleep(40);
            try (Span span2 = Span.start(new SpanInfo("sleep 3.1"))) {
                Thread.sleep(20);
            }
            Thread.sleep(10);
        }

        return "This has been a traced request";
    }
}
