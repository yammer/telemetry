package com.hypnoticocelot.rapper.agent;

import com.hypnoticocelot.rapper.agent.handlers.JaxRsMethodHandler;
import com.hypnoticocelot.rapper.agent.handlers.MethodInstrumentationHandler;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class RapperTransformer implements ClassFileTransformer {
    private final Set<MethodInstrumentationHandler> handlers = new HashSet<>();

    public void addHandler(MethodInstrumentationHandler handler) {
        handlers.add(handler);
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            ClassReader cr = new ClassReader(classfileBuffer);
            ClassNode classNode = new ClassNode();
            cr.accept(classNode, ClassReader.EXPAND_FRAMES);

            boolean classUpdated = false;
            final Iterable<MethodNode> methodNodes = classNode.methods;
            for (MethodNode methodNode : methodNodes) {
                for (MethodInstrumentationHandler handler : handlers) {
                    if (handler.likes(classNode, methodNode)) {
                        System.out.println("Instrumenting method: handler=" + handler.getClass().getName() +
                                "; class=" + classNode.name +
                                "; method=" + methodNode.name);
                        InsnList beginList = new InsnList();
                        beginList.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                        beginList.add(new LdcInsnNode("[BEGIN] YOUR ASS HAS BEEN INSTRUMENTED! " + methodNode.name));
                        beginList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V"));

                        methodNode.instructions.insert(beginList);

                        Iterator<AbstractInsnNode> insnNodes = methodNode.instructions.iterator();
                        while (insnNodes.hasNext()) {
                            AbstractInsnNode insn = insnNodes.next();

                            if (insn.getOpcode() == Opcodes.IRETURN
                                    || insn.getOpcode() == Opcodes.RETURN
                                    || insn.getOpcode() == Opcodes.ARETURN
                                    || insn.getOpcode() == Opcodes.LRETURN
                                    || insn.getOpcode() == Opcodes.DRETURN) {
                                InsnList endList = new InsnList();
                                endList.add(new FieldInsnNode(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;"));
                                endList.add(new LdcInsnNode("[END] YOUR ASS HAS BEEN INSTRUMENTED! " + methodNode.name));
                                endList.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream", "println", "(Ljava/lang/String;)V"));
                                methodNode.instructions.insertBefore(insn, endList);
                            }
                        }

                        classUpdated = true;
                        break;
                    }
                }
            }

            if (classUpdated) {
                ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
                classNode.accept(cw);

                return cw.toByteArray();
            } else {
                return null;
            }
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }
}
