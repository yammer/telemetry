package com.yammer.telemetry.agent;

public class LogConfiguration {
    private boolean enabled = false;
    private String file;

    public boolean isEnabled() {
        return enabled;
    }

    public String getFile() {
        return file;
    }
}
