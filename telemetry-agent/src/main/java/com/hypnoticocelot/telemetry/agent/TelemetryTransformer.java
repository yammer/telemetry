package com.hypnoticocelot.telemetry.agent;

import com.hypnoticocelot.telemetry.agent.handlers.MethodInstrumentationHandler;
import javassist.*;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.HashSet;
import java.util.Set;

public class TelemetryTransformer implements ClassFileTransformer {
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
            final String realClassName = className.replace('/', '.');
            ClassPool cp = ClassPool.getDefault();
            cp.insertClassPath(new LoaderClassPath(loader));
            cp.insertClassPath(new ByteArrayClassPath(realClassName, classfileBuffer));
            CtClass cc = cp.get(realClassName);

            boolean classUpdated = false;
            for (CtMethod method : cc.getMethods()) {
                for (MethodInstrumentationHandler handler : handlers) {
                    if (handler.likes(cc, method)) {
                        System.out.println("Instrumenting method: handler=" + handler.getClass().getName() +
                                "; class=" + cc.getName() +
                                "; method=" + method.getName());

                        method.insertBefore("com.hypnoticocelot.telemetry.agent.SpanHelper.startSpan(new com.hypnoticocelot.telemetry.tracing.SpanInfo(\"SPAN!\"));");
                        method.insertAfter("com.hypnoticocelot.telemetry.agent.SpanHelper.endSpan();", true);

                        classUpdated = true;
                        break;
                    }
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
