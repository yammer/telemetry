package com.yammer.telemetry.agent;

public class LogConfiguration {
    private boolean enabled = false;
    private String file = null;

    public LogConfiguration() {
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getFile() {
        return file;
    }
}
