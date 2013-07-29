package com.yammer.telemetry.agent;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class TelemetryConfiguration {
    private List<String> instruments = Collections.emptyList();
    private SinkConfiguration sinks = new SinkConfiguration();
    private Map<String, String> annotations = Collections.emptyMap();

    private TelemetryConfiguration() {
    }

    public List<String> getInstruments() {
        return instruments;
    }

    public SinkConfiguration getSinks() {
        return sinks;
    }

    public Map<String, String> getAnnotations() {
        return annotations;
    }

    public boolean isEnabled() {
        return (instruments.size() > 0) && sinks.isEnabled();
    }
}
