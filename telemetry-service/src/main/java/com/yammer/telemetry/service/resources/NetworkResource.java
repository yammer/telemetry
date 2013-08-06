package com.yammer.telemetry.service.resources;

import com.google.common.collect.ImmutableSet;
import com.yammer.telemetry.service.models.GefxData;
import com.yammer.telemetry.service.models.GefxEdge;
import com.yammer.telemetry.service.models.GefxNode;
import com.yammer.telemetry.tracing.*;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/data/network")
@Produces(MediaType.APPLICATION_JSON)
public class NetworkResource {
    private final SpanSource spanSource;

    public NetworkResource(SpanSource spanSource) {
        this.spanSource = spanSource;
    }

    @GET
    public GefxData getNetworkJson() {
        final ImmutableSet.Builder<GefxNode> nodeBuilder = new ImmutableSet.Builder<>();
        final ImmutableSet.Builder <GefxEdge> edgeBuilder = new ImmutableSet.Builder<>();

        for (Trace trace : spanSource.getTraces()) {
            processTrace(trace, trace.getRoot(), nodeBuilder, edgeBuilder);
        }

        return new GefxData(nodeBuilder.build(), edgeBuilder.build());
    }

    private void processTrace(Trace trace, SpanData span, ImmutableSet.Builder<GefxNode> nodeBuilder, ImmutableSet.Builder<GefxEdge> edgeBuilder) {
        for (AnnotationData annotationData : trace.getAnnotations(span)) {
            if (AnnotationNames.SERVICE_NAME.equals(annotationData.getName())) {
                nodeBuilder.add(new GefxNode(annotationData.getMessage(), annotationData.getMessage()));

                for (SpanData childSpan : trace.getChildren(span)) {
                    for (AnnotationData childAnnotationData : trace.getAnnotations(childSpan)) {
                        if (AnnotationNames.SERVICE_NAME.equals(childAnnotationData.getName())) {
                            edgeBuilder.add(new GefxEdge(annotationData.getMessage(), childAnnotationData.getMessage()));
                        }
                    }
                }
            }

            for (SpanData childSpan : trace.getChildren(span)) {
                processTrace(trace, childSpan, nodeBuilder, edgeBuilder);
            }
        }
    }
}
