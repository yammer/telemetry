package com.yammer.telemetry.test;

import com.yammer.telemetry.instrumentation.ClassInstrumentationHandler;
import com.yammer.telemetry.instrumentation.TelemetryTransformer;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.fail;

public class TelemetryTestHelpers {
    public static void runTransformed(Class<?> clazz, ClassInstrumentationHandler... handlers) throws Exception {
        Method[] methods = clazz.getDeclaredMethods();
        Map<Method, Exception> testFailures = new HashMap<>();
        int ran = 0;

        for (Method method : methods) {
            if (method.isAnnotationPresent(TransformedTest.class)) {
                try {
                    ran++;
                    runTransformed(clazz, method.getName(), handlers);
                } catch (Exception e) {
                    throw testFailures.put(method, e);
                }
            }
        }

        if (!testFailures.isEmpty()) {
            StringWriter builder = new StringWriter();
            PrintWriter writer = new PrintWriter(builder);
            writer.println("Transformed tests failed:");
            for (Map.Entry entry : testFailures.entrySet()) {
                writer.printf("%s:%n%s%n%n", entry.getKey(), entry.getValue());
            }
            fail(builder.toString());
        }

        if (ran == 0) {
            fail("No tests were found within '" + clazz.getName() + "' that were annotated as '" + TransformedTest.class.getName() + "'");
        }
    }

    public static void runTransformed(Class<?> clazz, String method, ClassInstrumentationHandler... handlers) throws Exception {
        TelemetryTransformer transformer = new TelemetryTransformer();
        for (ClassInstrumentationHandler handler : handlers) {
            transformer.addHandler(handler);
        }

        try (TransformingClassLoader loader = new TransformingClassLoader(transformer)) {
            Class<?> aClass = loader.loadClass(clazz.getName());
            Method declaredMethod = aClass.getDeclaredMethod(method);
            declaredMethod.invoke(null);
        }
    }
}
