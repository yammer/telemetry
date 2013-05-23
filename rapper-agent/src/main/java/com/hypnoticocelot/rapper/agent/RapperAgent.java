package com.hypnoticocelot.rapper.agent;

import com.hypnoticocelot.rapper.agent.handlers.JaxRsMethodHandler;

import java.lang.instrument.Instrumentation;

public class RapperAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        final RapperTransformer transformer = new RapperTransformer();
        transformer.addHandler(new JaxRsMethodHandler());
        inst.addTransformer(transformer);
    }
}
