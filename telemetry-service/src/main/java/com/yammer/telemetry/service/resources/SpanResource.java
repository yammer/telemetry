package com.yammer.telemetry.service.resources;

import com.yammer.telemetry.service.models.BeanAnnotationData;
import com.yammer.telemetry.tracing.SpanSink;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.math.BigInteger;

@Path("/spans/{traceId}/{spanId}")
public class SpanResource {
    private static final Logger LOG = LoggerFactory.getLogger(SpanResource.class);
    private final SpanSink sink;

    public SpanResource(SpanSink sink) {
        this.sink = sink;
    }

    @POST
    public void logAnnotation(@PathParam("traceId") BigInteger traceId,
                              @PathParam("spanId") BigInteger spanId,
                              @Valid BeanAnnotationData annotationData) {
        LOG.debug("Logging inbound annotation data (traceId={}, spanId={}): {}", traceId, spanId, annotationData);
        sink.recordAnnotation(traceId, spanId, annotationData);
    }
}
