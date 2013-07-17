package com.hypnoticocelot.telemetry.agent.handlers;

import javassist.*;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ConstPool;

public class JaxRsMethodHandler implements MethodInstrumentationHandler {
    @Override
    public boolean transformed(CtClass cc, CtMethod method, ClassPool pool) {
        try {
            if ((
                    cc.hasAnnotation(Class.forName("javax.ws.rs.Path")) ||
                    method.hasAnnotation(Class.forName("javax.ws.rs.Path"))
            ) && (
                    method.hasAnnotation(Class.forName("javax.ws.rs.GET")) ||
                    method.hasAnnotation(Class.forName("javax.ws.rs.PUT")) ||
                    method.hasAnnotation(Class.forName("javax.ws.rs.POST")) ||
                    method.hasAnnotation(Class.forName("javax.ws.rs.DELETE")) ||
                    method.hasAnnotation(Class.forName("javax.ws.rs.HEAD")) ||
                    method.hasAnnotation(Class.forName("javax.ws.rs.OPTIONS"))
            )) {
                try {
                    // javassist won't let you check for the availability of a field and fetching the field throws
                    // NotFoundException instead of returning null. So...ick.
                    cc.getField("_sr");
                } catch (NotFoundException nfe) {
                    ConstPool constPool = cc.getClassFile().getConstPool();
                    AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
                    javassist.bytecode.annotation.Annotation annotation = new javassist.bytecode.annotation.Annotation("javax.ws.rs.core.Context", constPool);
                    attr.setAnnotation(annotation);

                    CtField uriInfo = new CtField(pool.get("javax.servlet.http.HttpServletRequest"), "_sr", cc);
                    uriInfo.getFieldInfo().addAttribute(attr);
                    cc.addField(uriInfo);
                }

                cc.getClassPool().importPackage("com.hypnoticocelot.telemetry.tracing");
                cc.getClassPool().importPackage("com.hypnoticocelot.telemetry.agent");
                method.insertBefore(
                        "SpanInfo spanInfo = new SpanInfo(\"JAX-RS: \" + _sr.getMethod() + \" \" + _sr.getRequestURI());" +
                        "SpanHelper.startSpan(spanInfo);"
                );
                method.insertAfter("SpanHelper.endSpan();", true);

                return true;
            }

            return false;
        } catch (Exception e) {
            throw new RuntimeException("Unable to instrument method", e);
        }
    }
}
