package com.yammer.telemetry.agent;

public class SinkConfiguration {
    private TelemetryServiceConfiguration telemetry = new TelemetryServiceConfiguration();

    public SinkConfiguration() {
    }

    public TelemetryServiceConfiguration getTelemetry() {
        return telemetry;
    }

    public boolean isEnabled() {
        return telemetry.isEnabled();
    }
}
