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
                checkHost(cc, pool);
                checkRequest(cc, pool);
                checkResponse(cc, pool);

                cc.getClassPool().importPackage("com.hypnoticocelot.telemetry.tracing");
                cc.getClassPool().importPackage("com.hypnoticocelot.telemetry.agent");
                cc.getClassPool().importPackage("com.google.common.collect");
                cc.getClassPool().importPackage("java.util");
                method.insertBefore(
                        "Map annotations = ImmutableMap.of(\"hostname\",_hostName,\"hostip\",_hostIp);" +
                        "SpanInfo spanInfo = new SpanInfo(\"JAX-RS: \" + _sreq.getMethod() + \" \" + _sreq.getRequestURI(), annotations);" +
                        "SpanHelper.startSpan(spanInfo);" +
                        "_sres.setHeader(\"X-Telemetry-Traced\", \"true\");"
                );
                method.insertAfter("SpanHelper.endSpan();", true);

                return true;
            }

            return false;
        } catch (Exception e) {
            throw new RuntimeException("Unable to instrument method", e);
        }
    }

    private void checkHost(CtClass cc, ClassPool pool) throws CannotCompileException, NotFoundException {
        try {
            // javassist won't let you check for the availability of a field and fetching the field throws
            // NotFoundException instead of returning null. So...ick.
            cc.getField("_hostName");
        } catch (NotFoundException nfe) {
            CtField hostName = new CtField(pool.get("java.lang.String"), "_hostName", cc);
            CtField hostIp = new CtField(pool.get("java.lang.String"), "_hostIp", cc);

            cc.getClassPool().importPackage("java.net");
            cc.addField(hostName, "InetAddress.getLocalHost().getHostName()");
            cc.addField(hostIp, "InetAddress.getLocalHost().getHostAddress()");
        }
    }

    private void checkRequest(CtClass cc, ClassPool pool) throws CannotCompileException, NotFoundException {
        try {
            // javassist won't let you check for the availability of a field and fetching the field throws
            // NotFoundException instead of returning null. So...ick.
            cc.getField("_sreq");
        } catch (NotFoundException nfe) {
            ConstPool constPool = cc.getClassFile().getConstPool();
            AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            javassist.bytecode.annotation.Annotation annotation = new javassist.bytecode.annotation.Annotation("javax.ws.rs.core.Context", constPool);
            attr.setAnnotation(annotation);

            CtField request = new CtField(pool.get("javax.servlet.http.HttpServletRequest"), "_sreq", cc);
            request.getFieldInfo().addAttribute(attr);
            cc.addField(request);
        }
    }

    private void checkResponse(CtClass cc, ClassPool pool) throws CannotCompileException, NotFoundException {
        try {
            // javassist won't let you check for the availability of a field and fetching the field throws
            // NotFoundException instead of returning null. So...ick.
            cc.getField("_sres");
        } catch (NotFoundException nfe) {
            ConstPool constPool = cc.getClassFile().getConstPool();
            AnnotationsAttribute attr = new AnnotationsAttribute(constPool, AnnotationsAttribute.visibleTag);
            javassist.bytecode.annotation.Annotation annotation = new javassist.bytecode.annotation.Annotation("javax.ws.rs.core.Context", constPool);
            attr.setAnnotation(annotation);

            CtField response = new CtField(pool.get("javax.servlet.http.HttpServletResponse"), "_sres", cc);
            response.getFieldInfo().addAttribute(attr);
            cc.addField(response);
        }
    }
}
