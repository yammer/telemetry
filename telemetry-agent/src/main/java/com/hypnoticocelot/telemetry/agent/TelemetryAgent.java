package com.hypnoticocelot.telemetry.agent;

import com.hypnoticocelot.telemetry.agent.handlers.ApacheHttpClientMethodHandler;
import com.hypnoticocelot.telemetry.agent.handlers.JaxRsMethodHandler;
import com.hypnoticocelot.telemetry.sinks.SLF4JSpanSink;
import com.hypnoticocelot.telemetry.tracing.SpanSinkRegistry;

import java.lang.instrument.Instrumentation;

public class TelemetryAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        // TODO: This stuff needs to all be loaded from a configuration file or from the command line.
        final SLF4JSpanSink loggingSink = new SLF4JSpanSink();
//        final TelemetryServiceSpanSink serviceSink = new TelemetryServiceSpanSink();

        SpanSinkRegistry.register(loggingSink);
//        SpanSinkRegistry.register(serviceSink);

        final TelemetryTransformer transformer = new TelemetryTransformer();
        transformer.addHandler(new JaxRsMethodHandler());
        transformer.addHandler(new ApacheHttpClientMethodHandler());
        inst.addTransformer(transformer);
    }
}
