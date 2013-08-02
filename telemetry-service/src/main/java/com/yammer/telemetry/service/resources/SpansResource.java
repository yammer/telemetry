package com.yammer.telemetry.service.resources;

import com.yammer.telemetry.service.models.BeanSpanData;
import com.yammer.telemetry.tracing.SpanSink;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.MediaType;

@Path("/spans")
@Consumes(MediaType.APPLICATION_JSON)
public class SpansResource {
    private final SpanSink sink;

    public SpansResource(SpanSink sink) {
        this.sink = sink;
    }

    @POST
    public void logSpan(BeanSpanData span) {
        sink.record(span);
    }
}
