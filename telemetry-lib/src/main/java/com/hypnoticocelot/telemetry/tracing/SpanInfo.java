package com.hypnoticocelot.telemetry.tracing;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class SpanInfo {
    private final String name;
    private final ImmutableMap<String, String> annotations;

    public SpanInfo(String name) {
        this.name = name;
        annotations = ImmutableMap.of();
    }

    public SpanInfo(String name, Map<String, String> annotations) {
        this.name = name;
        this.annotations = ImmutableMap.copyOf(annotations);
    }

    public String getName() {
        return name;
    }

    public ImmutableMap<String, String> getAnnotations() {
        return annotations;
    }
}
