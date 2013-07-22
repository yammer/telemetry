package com.yammer.telemetry.agent;

public class SinkConfiguration {
    private SLF4JSinkConfiguration slf4j = new SLF4JSinkConfiguration();
    private TelemetryServiceConfiguration telemetry = new TelemetryServiceConfiguration();

    public SinkConfiguration() {
    }

    public SLF4JSinkConfiguration getSlf4j() {
        return slf4j;
    }

    public TelemetryServiceConfiguration getTelemetry() {
        return telemetry;
    }

    public boolean isEnabled() {
        return slf4j.isEnabled() || telemetry.isEnabled();
    }
}
