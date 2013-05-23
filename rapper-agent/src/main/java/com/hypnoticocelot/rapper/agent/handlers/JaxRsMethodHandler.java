package com.hypnoticocelot.rapper.agent.handlers;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.List;

public class JaxRsMethodHandler implements MethodInstrumentationHandler {
    @Override
    public boolean likes(ClassNode classNode, MethodNode methodNode) {
        return (
                annotatedWith(classNode.visibleAnnotations, "javax.ws.rs.Path") ||
                annotatedWith(methodNode.visibleAnnotations, "javax.ws.rs.Path")
        ) && (
                annotatedWith(methodNode.visibleAnnotations, "javax.ws.rs.GET") ||
                annotatedWith(methodNode.visibleAnnotations, "javax.ws.rs.PUT") ||
                annotatedWith(methodNode.visibleAnnotations, "javax.ws.rs.POST") ||
                annotatedWith(methodNode.visibleAnnotations, "javax.ws.rs.DELETE") ||
                annotatedWith(methodNode.visibleAnnotations, "javax.ws.rs.HEAD") ||
                annotatedWith(methodNode.visibleAnnotations, "javax.ws.rs.OPTIONS")
        );
    }

    private boolean annotatedWith(List<AnnotationNode> annotations, String annotationClass) {
        final String description = classToDescription(annotationClass);
        if (annotations != null) {
            for (AnnotationNode annotation : annotations) {
                if (annotation.desc.equals(description)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String classToDescription(String className) {
        return "L" + className.replace('.', '/') + ";";
    }
}
