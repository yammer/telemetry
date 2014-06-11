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

    @Test
    public void testSamplingValueOfString() {
        assertNull(Sampling.valueOf(null));
        assertNull(Sampling.valueOf("unknown"));

        assertEquals(Sampling.ON, Sampling.valueOf("on"));
        assertEquals(Sampling.ON, Sampling.valueOf("ON"));
        assertEquals(Sampling.ON, Sampling.valueOf("oN"));

        assertEquals(Sampling.OFF, Sampling.valueOf("off"));
        assertEquals(Sampling.OFF, Sampling.valueOf("OFF"));
        assertEquals(Sampling.OFF, Sampling.valueOf("oFf"));
    }
}