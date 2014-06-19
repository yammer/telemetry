package com.yammer.telemetry.tracing;

import java.math.BigInteger;
import java.util.Random;

public class IDGenerator {
    private static final Random ID_GENERATOR = new Random(System.currentTimeMillis());

    public BigInteger generateTraceId() {
        return new BigInteger(64, ID_GENERATOR);
    }

    public BigInteger generateSpanId() {
        return new BigInteger(32, ID_GENERATOR);
    }
}
