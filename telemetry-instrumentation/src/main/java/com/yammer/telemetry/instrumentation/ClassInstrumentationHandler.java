package com.yammer.telemetry.instrumentation;

import javassist.ClassPool;
import javassist.CtClass;

public interface ClassInstrumentationHandler {
    boolean transformed(CtClass cc, ClassPool pool);
}
