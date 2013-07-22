package com.yammer.telemetry.service.views;

import com.yammer.telemetry.tracing.Trace;
import com.yammer.dropwizard.views.View;

public class TracingHomeView extends View {
    private final Iterable<Trace> traces;

    public TracingHomeView(Iterable<Trace> traces) {
        super("home.ftl");
        this.traces = traces;
    }

    public Iterable<Trace> getTraces() {
        return traces;
    }
}
