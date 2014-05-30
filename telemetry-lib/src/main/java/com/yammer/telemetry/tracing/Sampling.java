package com.yammer.telemetry.tracing;

public abstract class Sampling {
    public static final Sampling ON = new SamplingOn();
    public static final Sampling OFF = new SamplingOff();

    public abstract boolean trace();

    private static class SamplingOff extends Sampling {
        @Override
        public boolean trace() {
            return false;
        }
    }

    private static class SamplingOn extends Sampling {
        @Override
        public boolean trace() {
            return true;
        }
    }
}
