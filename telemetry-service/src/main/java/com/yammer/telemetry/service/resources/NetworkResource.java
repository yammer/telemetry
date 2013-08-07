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
import java.util.List;

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
        String serviceName = determineServiceName(trace.getAnnotations(span));
        nodeBuilder.add(new GefxNode(serviceName, serviceName));

        for (SpanData childSpan : trace.getChildren(span)) {
            String childServiceName = determineServiceName(trace.getAnnotations(childSpan));
            if (childServiceName != null) {
                edgeBuilder.add(new GefxEdge(serviceName, childServiceName));
            }

            processTrace(trace, childSpan, nodeBuilder, edgeBuilder);
        }
    }

    private String determineServiceName(List<AnnotationData> annotations) {
        String serviceName = null;
        for (AnnotationData annotation : annotations) {
            if (AnnotationNames.SERVICE_NAME.equals(annotation.getName())) {
                serviceName = annotation.getMessage();
            } else if (serviceName == null && AnnotationNames.FALLBACK_SERVICE_NAME.equals(annotation.getName())) {
                serviceName = annotation.getMessage();
            }
        }
        return serviceName;
    }
}
