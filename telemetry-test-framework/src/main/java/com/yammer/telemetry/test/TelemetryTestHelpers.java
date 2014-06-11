package com.yammer.telemetry.test;

import com.yammer.telemetry.instrumentation.ClassInstrumentationHandler;
import com.yammer.telemetry.instrumentation.TelemetryTransformer;
import org.junit.Before;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.fail;

public class TelemetryTestHelpers {
    public static void runTransformed(Class<?> clazz, ClassInstrumentationHandler... handlers) throws Exception {
        Method[] methods = clazz.getDeclaredMethods();
        Map<Method, Throwable> testFailures = new HashMap<>();
        int ran = 0;

        for (Method method : methods) {
            if (method.isAnnotationPresent(TransformedTest.class)) {
                try {
                    ran++;
                    runTransformed(clazz, method.getName(), handlers);
                } catch (Exception e) {
                    //noinspection ThrowableResultOfMethodCallIgnored
                    testFailures.put(method, unwrap(e));
                }
            }
        }

        if (!testFailures.isEmpty()) {
            StringWriter builder = new StringWriter();
            PrintWriter writer = new PrintWriter(builder);
            writer.println("Transformed tests failed:");
            for (Map.Entry<Method, Throwable> entry : testFailures.entrySet()) {
                writer.printf("%s:%n%s%n%n", entry.getKey(), entry.getValue());
                //noinspection ThrowableResultOfMethodCallIgnored
                entry.getValue().printStackTrace(writer);
                writer.println();
            }
            fail(builder.toString());
        }

        if (ran == 0) {
            fail("No tests were found within '" + clazz.getName() + "' that were annotated as '" + TransformedTest.class.getName() + "'");
        }
    }

    private static Throwable unwrap(Throwable e) {
        if (e instanceof InvocationTargetException) {
            return e.getCause();
        }
        return e;
    }

    public static void runTransformed(Class<?> clazz, String method, ClassInstrumentationHandler... handlers) throws Exception {
        Set<String> befores = new HashSet<>();
        for (Method beforeMethod : clazz.getDeclaredMethods()) {
            if (beforeMethod.isAnnotationPresent(Before.class)) {
                befores.add(beforeMethod.getName());
            }
        }

        final TelemetryTransformer transformer = new TelemetryTransformer();
        for (ClassInstrumentationHandler handler : handlers) {
            transformer.addHandler(handler);
        }

        try (TransformingClassLoader loader = AccessController.doPrivileged(new PrivilegedAction<TransformingClassLoader>() {
            @Override
            public TransformingClassLoader run() {
                return new TransformingClassLoader(transformer);
            }
        })) {
            Class<?> aClass = loader.loadClass(clazz.getName());
            Object instance = aClass.newInstance();
            for (String beforeMethod : befores) {
                Method before = aClass.getDeclaredMethod(beforeMethod);
                before.invoke(instance);
            }
            Method declaredMethod = aClass.getDeclaredMethod(method);
            declaredMethod.invoke(instance);
        }
    }
}
