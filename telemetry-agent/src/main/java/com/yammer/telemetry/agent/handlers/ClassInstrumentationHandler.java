package com.yammer.telemetry.agent.handlers;

import javassist.ClassPool;
import javassist.CtClass;

public interface ClassInstrumentationHandler {
    boolean transformed(CtClass cc, ClassPool pool);
}
