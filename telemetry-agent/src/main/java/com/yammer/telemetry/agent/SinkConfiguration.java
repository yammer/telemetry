package com.yammer.telemetry.agent;

public class SinkConfiguration {
    private LogConfiguration log = new LogConfiguration();

    public LogConfiguration getLog() { return log; }

    public boolean isEnabled() {
        return log.isEnabled();
    }
}
