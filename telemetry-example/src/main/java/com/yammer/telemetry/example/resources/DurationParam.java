package com.yammer.telemetry.example.resources;

import com.yammer.dropwizard.util.Duration;

public class DurationParam {
    private final Duration duration;

    public DurationParam(String durationString) {
        duration = Duration.parse(durationString);
    }

    public Duration getDuration() {
        return duration;
    }
}
