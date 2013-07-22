package com.yammer.telemetry.agent;

import java.util.Collections;
import java.util.List;

public class TelemetryConfiguration {
    private List<String> instruments = Collections.emptyList();
    private SinkConfiguration sinks = new SinkConfiguration();

    private TelemetryConfiguration() {
    }

    public List<String> getInstruments() {
        return instruments;
    }

    public SinkConfiguration getSinks() {
        return sinks;
    }

    public boolean isEnabled() {
        return (instruments.size() > 0) && sinks.isEnabled();
    }
}
