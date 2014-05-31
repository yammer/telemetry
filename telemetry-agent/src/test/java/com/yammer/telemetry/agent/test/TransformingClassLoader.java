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

    public TransformingClassLoader(Class<?> clazzToInstrument, TelemetryTransformer transformer) {
        super(new URL[] {clazzToInstrument.getProtectionDomain().getCodeSource().getLocation()});
        this.transformer = checkNotNull(transformer);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try (InputStream classStream = super.getResourceAsStream(name.replace('.', '/') + ".class")) {

            ClassPool classPool = new ClassPool(null);
            classPool.appendSystemPath();

            byte[] transformedBytes = transformer.transform(this, name, ByteStreams.toByteArray(classStream), classPool);

            if (transformedBytes == null)
                return super.loadClass(name);
            else
                return super.defineClass(name, transformedBytes, 0, transformedBytes.length);
        } catch (IOException | IllegalClassFormatException | RuntimeException e) {
            throw new ClassNotFoundException(name, e);
        }
    }
}
