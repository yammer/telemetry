package com.yammer.telemetry.agent.handlers;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

public interface MethodInstrumentationHandler {
    boolean transformed(CtClass cc, CtMethod method, ClassPool pool);
}
