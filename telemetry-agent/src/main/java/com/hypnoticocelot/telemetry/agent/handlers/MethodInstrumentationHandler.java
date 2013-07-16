package com.hypnoticocelot.telemetry.agent.handlers;

import javassist.CtClass;
import javassist.CtMethod;

public interface MethodInstrumentationHandler {
    boolean likes(CtClass classNode, CtMethod methodNode);
}
