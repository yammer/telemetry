package com.yammer.telemetry.service.resources;

import com.google.common.collect.ImmutableList;
import com.yammer.telemetry.tracing.SpanData;
import com.yammer.telemetry.tracing.Trace;
import com.yammer.telemetry.service.api.TreeSpan;
import com.yammer.telemetry.tracing.SpanSource;
import com.yammer.telemetry.service.api.Tree;
import org.joda.time.DateTime;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/data/trees")
@Produces(MediaType.APPLICATION_JSON)
public class TreesResource {
    private final SpanSource spanSource;

    public TreesResource(SpanSource spanSource) {
        this.spanSource = spanSource;
    }

    @GET
    public Collection<Tree> getTrees() {
        final Collection<Trace> traces = spanSource.getTraces();
        final ImmutableList.Builder<Tree> builder = new ImmutableList.Builder<>();
        for (Trace trace : traces) {
            Tree tree = traceToTree(trace);
            if (tree != null) builder.add(tree);
        }
        return builder.build();
    }

    private Tree traceToTree(Trace trace) {
        if (trace.getRoot() == null) return null;
        return new Tree(trace.getId(), treeSpanFor(trace.getRoot(), trace));
    }

    private TreeSpan treeSpanFor(SpanData spanData, Trace trace) {
        final ImmutableList.Builder<TreeSpan> childBuilder = new ImmutableList.Builder<>();
        for (SpanData child : trace.getChildren(spanData)) {
            childBuilder.add(treeSpanFor(child, trace));
        }
        final ImmutableList<TreeSpan> children = childBuilder.build();

        return new TreeSpan(spanData.getId(),
                spanData.getName(),
                new DateTime(spanData.getStartTimeNanos()),
                spanData.getDuration(),
                trace.getAnnotations(spanData),
                children);
    }
}
