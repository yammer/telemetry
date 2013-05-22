package com.hypnoticocelot.rapper.dropwizard.views;

import com.hypnoticocelot.rapper.tracing.Trace;
import com.yammer.dropwizard.views.View;

public class TraceView extends View {
    private final Trace trace;

    public TraceView(Trace trace) {
        super("trace.ftl");
        this.trace = trace;
    }

    public Trace getTrace() {
        return trace;
    }
}
