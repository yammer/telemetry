package com.yammer.telemetry.test;

import com.google.common.io.ByteStreams;
import com.yammer.telemetry.instrumentation.TelemetryTransformer;
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

    public TransformingClassLoader(TelemetryTransformer transformer) {
        super(new URL[] {});
        this.transformer = checkNotNull(transformer);
        classPool = new ClassPool(null);
        classPool.appendSystemPath();
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        Class<?> loadedClass = super.findLoadedClass(name);
        if (loadedClass != null) return loadedClass;
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
