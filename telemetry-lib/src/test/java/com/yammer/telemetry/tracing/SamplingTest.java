package com.yammer.telemetry.tracing;

import org.junit.Test;

import static org.junit.Assert.*;

public class SamplingTest {
    @Test
    public void testSamplingOffNeverSamples() {
        Sampling sampler = Sampling.OFF;

        for (int i = 0; i < 10; i++) {
            assertFalse(sampler.trace());
        }
    }

    @Test
    public void testSamplingOnAlwaysSamples() {
        Sampling sampler = Sampling.ON;

        for (int i = 0; i < 10; i++) {
            assertTrue(sampler.trace());
        }
    }
}