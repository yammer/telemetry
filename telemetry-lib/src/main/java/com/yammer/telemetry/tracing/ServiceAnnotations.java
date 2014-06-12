package com.yammer.telemetry.tracing;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.lang.management.ManagementFactory;

public class ServiceAnnotations {
    public String service;
    public int pid;
    public String host;

    public ServiceAnnotations() {
        initPidAndHost();
    }

    public ServiceAnnotations(String service) {
        this.service = service;
        initPidAndHost();
    }

    private void initPidAndHost() {
        String pidAndHostString = ManagementFactory.getRuntimeMXBean().getName();
        if (pidAndHostString != null) {
            String[] pidAndHost = pidAndHostString.split("@");
            if (pidAndHost.length == 2) {
                try {
                    this.pid = Integer.parseInt(pidAndHost[0]);
                } catch (NumberFormatException ignored) {
                }
                this.host = pidAndHost[1];
            } else {
                this.host = pidAndHostString;
            }
        }
    }

    public String getService() {
        return service;
    }

    @JsonIgnore
    public int getPid() {
        return pid;
    }

    @JsonIgnore
    public String getHost() {
        return host;
    }
}
