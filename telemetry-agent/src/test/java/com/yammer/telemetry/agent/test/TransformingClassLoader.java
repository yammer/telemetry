package com.yammer.telemetry.agent.test;

import com.google.common.io.ByteStreams;
import com.yammer.telemetry.agent.TelemetryTransformer;

import java.io.IOException;
import java.io.InputStream;
import java.lang.instrument.IllegalClassFormatException;

import static com.google.common.base.Preconditions.checkNotNull;

public class TransformingClassLoader extends ClassLoader {
    private final TelemetryTransformer transformer;

    public TransformingClassLoader(TelemetryTransformer transformer) {
        this.transformer = checkNotNull(transformer);
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        try (InputStream classStream = super.getResourceAsStream(name.replace('.', '/') + ".class")) {

            byte[] transformedBytes = transformer.transform(this, name, null, null, ByteStreams.toByteArray(classStream));

            if (transformedBytes == null)
                return super.loadClass(name);
            else
                return super.defineClass(name, transformedBytes, 0, transformedBytes.length);
        } catch (IOException | IllegalClassFormatException | RuntimeException e) {
            throw new ClassNotFoundException(name, e);
        }
    }
}
