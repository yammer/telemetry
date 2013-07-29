package com.yammer.telemetry.agent;

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

            cc.getClassPool().importPackage("java.util");
            cc.getClassPool().importPackage("com.yammer.telemetry.tracing");
            cc.getClassPool().importPackage("com.yammer.telemetry.agent");

            StringBuilder body = new StringBuilder();
            body.append("{");

            body.append("    Span span = Span.start(").append(spanName);
            if (annotationMap != null) {
                body.append(", ").append(annotationMap);
            } else {
                body.append(", Collections.emptyMap()");
            }

            if (deriveTraceId == null || deriveParentSpanId == null) {
                body.append(");");
            } else {
                body.append(", ").append(deriveTraceId).append(", ").append(deriveParentSpanId).append(");");
            }

            body.append("    try {");
            if (extraCode != null) {
                body.append("    ").append(extraCode);
            }

            if (method.getReturnType().getClass().equals(Void.class)) {
                body.append("        ").append(copiedMethodName).append("($$);");
            } else {
                body.append("        return ").append(copiedMethodName).append("($$);");
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
