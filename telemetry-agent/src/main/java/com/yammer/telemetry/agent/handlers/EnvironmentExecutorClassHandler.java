package com.yammer.telemetry.agent.handlers;

import javassist.*;

import java.io.IOException;

import static com.yammer.telemetry.agent.handlers.MetricsRegistryHandler.switchImplementation;

public class EnvironmentExecutorClassHandler extends SubTypeInstrumentationHandler {
    public EnvironmentExecutorClassHandler() {
        super("com.yammer.dropwizard.config.Environment");
    }

    @Override
    protected boolean transform(CtClass cc, ClassPool pool) throws NotFoundException, CannotCompileException, IOException {
        if ("com.yammer.dropwizard.config.Environment".equals(cc.getName())) {
            switchImplementation(cc, pool, "java.util.concurrent.ThreadPoolExecutor", "com.yammer.telemetry.agent.handlers.InstrumentedThreadPoolExecutor");
            switchImplementation(cc, pool, "java.util.concurrent.ScheduledThreadPoolExecutor", "com.yammer.telemetry.agent.handlers.InstrumentedScheduledThreadPoolExecutor");
            return true;
        }
        return false;
    }
}
