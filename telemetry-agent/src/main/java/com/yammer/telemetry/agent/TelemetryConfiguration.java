package com.yammer.telemetry.agent;

import com.yammer.telemetry.tracing.Sampling;
import com.yammer.telemetry.tracing.ServiceAnnotations;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;

public class TelemetryConfiguration {
    private List<String> instruments = Collections.emptyList();
    private SinkConfiguration sinks = new SinkConfiguration();
    private ServiceAnnotations annotations = new ServiceAnnotations("unknown");

    @NotNull
    private Sampling sampler = Sampling.ON;

    public List<String> getInstruments() {
        return instruments;
    }

    public SinkConfiguration getSinks() {
        return sinks;
    }

    public ServiceAnnotations getAnnotations() {
        return annotations;
    }

    public boolean isEnabled() {
        return (instruments.size() > 0) && sinks.isEnabled();
    }

    public void setSampler(String sampler) {
        this.sampler = Sampling.valueOf(sampler);
    }
    public Sampling getSampler() {
        return sampler;
    }
}
