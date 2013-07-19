package com.hypnoticocelot.telemetry.agent;

import javassist.*;

public class BytecodeHelper {
    public static void spanMethod(CtClass cc, CtMethod method, String spanName, String annotationMap, String extraCode) {
        String originalMethodName = method.getName();
        String copiedMethodName = originalMethodName + "$TelemetryImpl";

        try {
            CtMethod copiedMethod = CtNewMethod.copy(method, copiedMethodName, cc, null);
            copiedMethod.setModifiers(Modifier.PRIVATE);
            cc.addMethod(copiedMethod);

            cc.getClassPool().importPackage("com.hypnoticocelot.telemetry.tracing");
            cc.getClassPool().importPackage("com.hypnoticocelot.telemetry.agent");

            StringBuffer body = new StringBuffer();
            body.append("{");

            if (extraCode != null) {
                body.append("    " + extraCode);
            }

            if (annotationMap != null) {
                body.append("    SpanInfo info = new SpanInfo(" + spanName + ", " + annotationMap + ");");
            } else {
                body.append("    SpanInfo info = new SpanInfo(" + spanName + ");");
            }

            body.append("    Span span = Span.start(info);");
            body.append("    try {");
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
        } catch (CannotCompileException e) {
            throw new RuntimeException("Unable to wrap method " + cc.getName() + "." + originalMethodName, e);
        } catch (NotFoundException e) {
            throw new RuntimeException("Unable to wrap method " + cc.getName() + "." + originalMethodName, e);
        }
    }
}
