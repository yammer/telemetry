package com.yammer.telemetry.service.views;

import com.yammer.telemetry.tracing.Trace;
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
