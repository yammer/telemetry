package com.hypnoticocelot.telemetry.agent.handlers;

import com.hypnoticocelot.telemetry.agent.BytecodeHelper;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;

import java.lang.reflect.Modifier;

public class ApacheHttpClientMethodHandler implements MethodInstrumentationHandler {
    @Override
    public boolean transformed(CtClass cc, CtMethod method, ClassPool pool) {
        try {
            if (cc.subtypeOf(pool.get("org.apache.http.client.HttpClient")) &&
                    "execute".equals(method.getName()) &&
                    !Modifier.isAbstract(method.getModifiers())) {
                String requestParameter = null;
                CtClass[] parameterTypes = method.getParameterTypes();
                for (int i = 0; i < parameterTypes.length; i++) {
                    if (parameterTypes[i].subtypeOf(pool.get("org.apache.http.HttpRequest"))) {
                        requestParameter = "$" + (i + 1);
                        break;
                    }
                }

                pool.importPackage("com.google.common.collect");
                BytecodeHelper.spanMethod(cc,
                        method,
                        "\"HTTP Request\"",
                        null,
                        requestParameter + ".setHeader(\"X-Telemetry-TraceId\", Span.currentTraceId().toString());" +
                        requestParameter + ".setHeader(\"X-Telemetry-Parent-SpanId\", Span.currentSpanId().toString());",
                        null,
                        null
                );

                return true;
            }

            return false;
        } catch (NotFoundException e) {
            throw new RuntimeException("Unable to instrument HttpClient", e);
        }
    }
}
