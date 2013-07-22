package com.yammer.telemetry.agent;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.yammer.telemetry.agent.handlers.ApacheHttpClientMethodHandler;
import com.yammer.telemetry.agent.handlers.JaxRsMethodHandler;
import com.yammer.telemetry.sinks.SLF4JSpanSink;
import com.yammer.telemetry.sinks.TelemetryServiceSpanSink;
import com.yammer.telemetry.tracing.SpanSinkRegistry;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.instrument.Instrumentation;

public class TelemetryAgent {
    public static void agentmain(String agentArgs, Instrumentation inst) {
        premain(agentArgs, inst);
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        if (!agentArgs.isEmpty()) {
            try {
                TelemetryConfiguration config = loadConfiguration(agentArgs);

                if (config.isEnabled()) {
                    if (config.getSinks().getSlf4j().isEnabled()) {
                        SpanSinkRegistry.register(new SLF4JSpanSink());
                    }

                    TelemetryServiceConfiguration telemetry = config.getSinks().getTelemetry();
                    if (telemetry.isEnabled()) {
                        SpanSinkRegistry.register(new TelemetryServiceSpanSink(telemetry.getHost(), telemetry.getPort()));
                    }

                    final TelemetryTransformer transformer = new TelemetryTransformer();
                    if (config.getInstruments().contains("inbound-http")) {
                        transformer.addHandler(new JaxRsMethodHandler());
                    }
                    if (config.getInstruments().contains("outbound-http")) {
                        transformer.addHandler(new ApacheHttpClientMethodHandler());
                    }
                    inst.addTransformer(transformer);
                }
            } catch (IOException e) {
                System.err.println("Failed to load telemetry agent configuration: " + e.toString());
                System.err.println("Application will continue uninstrumented.");
                e.printStackTrace(System.err);
            }
        } else {
            System.err.println("No agent configuration path was specified. Application will not be traced by telemetry.");
            System.err.println("To trace an application, specify the path to telemetry.yml as an argument to the agent.");
        }
    }

    private static TelemetryConfiguration loadConfiguration(String filePath) throws IOException {
        File configPath = new File(filePath);
        if (!configPath.isFile()) {
            throw new FileNotFoundException("Config file " + filePath + " does not exist");
        }

        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(configPath, TelemetryConfiguration.class);
    }
}
