package com.yammer.telemetry.agent;

public class SinkConfiguration {
    private TelemetryServiceConfiguration telemetry = new TelemetryServiceConfiguration();
    private LogConfiguration log = new LogConfiguration();

    public SinkConfiguration() {
    }

    public TelemetryServiceConfiguration getTelemetry() {
        return telemetry;
    }

    public LogConfiguration getLog() { return log; }

    public boolean isEnabled() {
        return telemetry.isEnabled() || log.isEnabled();
    }
}
