package com.yammer.telemetry.tracing;

public class Annotations {
    private static ServiceAnnotations serviceAnnotations = new ServiceAnnotations();

    public static void setServiceAnnotations(ServiceAnnotations serviceAnnotations) {
        Annotations.serviceAnnotations = serviceAnnotations;
    }

    public static ServiceAnnotations getServiceAnnotations() {
        return serviceAnnotations;
    }
}
