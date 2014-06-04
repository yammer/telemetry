package com.yammer.telemetry.agent.test;

import com.google.common.io.ByteStreams;
import com.yammer.telemetry.agent.TelemetryTransformer;
import javassist.ClassPool;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;
import java.net.URL;
import java.net.URLClassLoader;

import static com.google.common.base.Preconditions.checkNotNull;

public class TransformingClassLoader extends URLClassLoader {
    private final TelemetryTransformer transformer;
    private final ClassPool classPool;

    public TransformingClassLoader(Class<?> clazzToInstrument, TelemetryTransformer transformer) {
        super(new URL[] {});
//        super(new URL[] {clazzToInstrument.getProtectionDomain().getCodeSource().getLocation()});
        this.transformer = checkNotNull(transformer);
        classPool = new ClassPool(null);
        classPool.appendSystemPath();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> loadedClass = super.findLoadedClass(name);
        if (loadedClass != null) return loadedClass;
        System.out.println("Loading: " + name);
        try (InputStream classStream = super.getResourceAsStream(name.replace('.', '/') + ".class")) {
            byte[] classfileBuffer = ByteStreams.toByteArray(classStream);
            byte[] transformedBytes = transformer.transform(this, name, classfileBuffer, classPool);

            if (transformedBytes == null) {
                if (name.startsWith("java")) {
                    return super.loadClass(name);
                }
                return super.defineClass(name, classfileBuffer, 0, classfileBuffer.length);
            } else {
                return super.defineClass(name, transformedBytes, 0, transformedBytes.length);
            }
        } catch (IOException | IllegalClassFormatException | RuntimeException e) {
            throw new ClassNotFoundException(name, e);
        }
    }
}
