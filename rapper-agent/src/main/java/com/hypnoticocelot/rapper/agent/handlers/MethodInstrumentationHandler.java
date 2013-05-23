package com.hypnoticocelot.rapper.agent.handlers;

import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;

public interface MethodInstrumentationHandler {
    boolean likes(ClassNode classNode, MethodNode methodNode);
}
