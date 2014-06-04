package com.yammer.telemetry.instrumentation;

import javassist.ByteArrayClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.LoaderClassPath;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

public class TelemetryTransformer implements ClassFileTransformer {
    private final Set<ClassInstrumentationHandler> handlers = new HashSet<>();

    public void addHandler(ClassInstrumentationHandler handler) {
        handlers.add(handler);
    }

    @Override
    public byte[] transform(ClassLoader loader,
                            String className,
                            Class<?> classBeingRedefined,
                            ProtectionDomain protectionDomain,
                            byte[] classfileBuffer) throws IllegalClassFormatException {
        return transform(loader, className, classfileBuffer, ClassPool.getDefault());
    }

    /**
     * Allows specifying the ClassPool, this allows tests to essentially 'reload' classes.
     *
     * @param loader
     * @param className
     * @param classfileBuffer
     * @param cp
     * @return
     * @throws IllegalClassFormatException
     */
    public byte[] transform(ClassLoader loader,
                            String className,
                            byte[] classfileBuffer,
                            ClassPool cp) throws IllegalClassFormatException {
        try {
            final String realClassName = className.replace('/', '.');
            cp.insertClassPath(new LoaderClassPath(loader));
            cp.insertClassPath(new ByteArrayClassPath(realClassName, classfileBuffer));
            CtClass cc = cp.get(realClassName);

            boolean classUpdated = false;
            for (ClassInstrumentationHandler handler : handlers) {
                if (classUpdated = handler.transformed(cc, cp)) {
                    break;
                }
            }

            if (classUpdated) {
                return cc.toBytecode();
            } else {
                return null;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        } catch (Throwable t) {
            t.printStackTrace();
            throw t;
        }
    }
}
