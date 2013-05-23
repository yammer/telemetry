package com.hypnoticocelot.telemetry.agent;

import com.hypnoticocelot.telemetry.agent.handlers.JaxRsMethodHandler;

import java.lang.instrument.Instrumentation;

public class TelemetryAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        final TelemetryTransformer transformer = new TelemetryTransformer();
        transformer.addHandler(new JaxRsMethodHandler());
        inst.addTransformer(transformer);
    }
}
