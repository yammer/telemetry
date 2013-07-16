package com.hypnoticocelot.telemetry.agent.handlers;

import javassist.CtClass;
import javassist.CtMethod;

public class JaxRsMethodHandler implements MethodInstrumentationHandler {
    @Override
    public boolean likes(CtClass classNode, CtMethod methodNode) {
        try {
            return (
                    classNode.hasAnnotation(Class.forName("javax.ws.rs.Path")) ||
                    methodNode.hasAnnotation(Class.forName("javax.ws.rs.Path"))
            ) && (
                    methodNode.hasAnnotation(Class.forName("javax.ws.rs.GET")) ||
                    methodNode.hasAnnotation(Class.forName("javax.ws.rs.PUT")) ||
                    methodNode.hasAnnotation(Class.forName("javax.ws.rs.POST")) ||
                    methodNode.hasAnnotation(Class.forName("javax.ws.rs.DELETE")) ||
                    methodNode.hasAnnotation(Class.forName("javax.ws.rs.HEAD")) ||
                    methodNode.hasAnnotation(Class.forName("javax.ws.rs.OPTIONS"))
            );
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
