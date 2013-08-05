package com.yammer.telemetry.agent;

public class Annotations {
    private static ServiceAnnotations serviceAnnotations;

    public static void setServiceAnnotations(ServiceAnnotations serviceAnnotations) {
        Annotations.serviceAnnotations = serviceAnnotations;
    }

    public static ServiceAnnotations getServiceAnnotations() {
        return serviceAnnotations;
    }
}
