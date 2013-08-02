package com.yammer.telemetry.service.resources;

import com.yammer.telemetry.service.models.BeanAnnotationData;
import com.yammer.telemetry.tracing.SpanSink;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.UUID;

@Path("/spans/{id}")
public class SpanResource {
    private final SpanSink sink;

    public SpanResource(SpanSink sink) {
        this.sink = sink;
    }

    @POST
    public void logAnnotation(@PathParam("id") UUID spanId, BeanAnnotationData annotation) {
        sink.recordAnnotation(spanId, annotation);
    }
}
