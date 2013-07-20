package com.hypnoticocelot.telemetry.agent;

import javassist.*;

public class BytecodeHelper {
    public static void spanMethod(CtClass cc,
                                  CtMethod method,
                                  String spanName,
                                  String annotationMap,
                                  String extraCode,
                                  String deriveTraceId,
                                  String deriveParentSpanId) {
        String originalMethodName = method.getName();
        String copiedMethodName = originalMethodName + "$TelemetryImpl";

        try {
            CtMethod copiedMethod = CtNewMethod.copy(method, copiedMethodName, cc, null);
            copiedMethod.setModifiers(Modifier.PRIVATE);
            cc.addMethod(copiedMethod);

            cc.getClassPool().importPackage("com.hypnoticocelot.telemetry.tracing");
            cc.getClassPool().importPackage("com.hypnoticocelot.telemetry.agent");

            StringBuilder body = new StringBuilder();
            body.append("{");

            if (annotationMap != null) {
                body.append("    SpanInfo info = new SpanInfo(" + spanName + ", " + annotationMap + ");");
            } else {
                body.append("    SpanInfo info = new SpanInfo(" + spanName + ");");
            }

            if (deriveTraceId == null || deriveParentSpanId == null) {
                body.append("    Span span = Span.start(info);");
            } else {
                body.append("    Span span = Span.start(info, " + deriveTraceId + ", " + deriveParentSpanId + ");");
            }

            body.append("    try {");
            if (extraCode != null) {
                body.append("    " + extraCode);
            }

            if (method.getReturnType().getClass().equals(Void.class)) {
                body.append("        " + copiedMethodName + "($$);");
            } else {
                body.append("        return " + copiedMethodName + "($$);");
            }
            body.append("    } finally {");
            body.append("        span.end();");
            body.append("    }");
            body.append("}");

            method.setBody(body.toString());
        } catch (CannotCompileException | NotFoundException e) {
            throw new RuntimeException("Unable to wrap method " + cc.getName() + "." + originalMethodName, e);
        }
    }
}
