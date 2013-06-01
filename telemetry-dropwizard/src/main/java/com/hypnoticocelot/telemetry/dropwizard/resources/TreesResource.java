package com.hypnoticocelot.telemetry.dropwizard.resources;

import com.google.common.collect.ImmutableList;
import com.hypnoticocelot.telemetry.tracing.Trace;
import com.hypnoticocelot.telemetry.dropwizard.api.TreeSpan;
import com.hypnoticocelot.telemetry.tracing.Span;
import com.hypnoticocelot.telemetry.tracing.SpanSource;
import com.hypnoticocelot.telemetry.dropwizard.api.Tree;
import org.joda.time.DateTime;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

@Path("/tracing/trees")
@Produces(MediaType.APPLICATION_JSON)
public class TreesResource {
    private SpanSource spanSource;

    public TreesResource(SpanSource spanSource) {
        this.spanSource = spanSource;
    }

    @GET
    public Collection<Tree> getTrees() {
        final Collection<Trace> traces = spanSource.getTraces();
        final ImmutableList.Builder<Tree> builder = new ImmutableList.Builder<>();
        for (Trace trace : traces) {
            builder.add(traceToTree(trace));
        }
        return builder.build();
    }

    private Tree traceToTree(Trace trace) {
        return new Tree(trace.getId(), treeSpanFor(trace.getRoot(), trace));
    }

    private TreeSpan treeSpanFor(Span span, Trace trace) {
        final ImmutableList.Builder<TreeSpan> childBuilder = new ImmutableList.Builder<>();
        for (Span child : trace.getChildren(span)) {
            childBuilder.add(treeSpanFor(child, trace));
        }
        final ImmutableList<TreeSpan> children = childBuilder.build();

        return new TreeSpan(span.getId(),
                span.getData().getName(),
                new DateTime(span.getStartTime()),
                new DateTime(span.getEndTime()),
                children.toArray(new TreeSpan[children.size()]));
    }
}
