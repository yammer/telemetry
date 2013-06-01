package com.hypnoticocelot.telemetry;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

public class SpanData {
    private final String name;
    private final ImmutableMap<String, String> annotations;

    public SpanData(String name) {
        this.name = name;
        annotations = ImmutableMap.of();
    }

    public SpanData(String name, Map<String, String> annotations) {
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
