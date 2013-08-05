package com.yammer.telemetry.agent;

public class ServiceAnnotations {
    public String service;

    private ServiceAnnotations() { }

    public ServiceAnnotations(String service) {
        this.service = service;
    }

    public String getService() {
        return service;
    }
}
