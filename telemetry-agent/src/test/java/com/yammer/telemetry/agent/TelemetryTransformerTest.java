package com.yammer.telemetry.agent;

import com.yammer.telemetry.agent.handlers.ClassInstrumentationHandler;
import com.yammer.telemetry.agent.test.TransformingClassLoader;
import javassist.*;
import org.junit.Test;

import static com.yammer.telemetry.agent.TelemetryTransformerTest.ClassUtils.wrapMethod;
import static org.junit.Assert.*;

public class TelemetryTransformerTest {
    @Test
    public void testUnmodifiedWhenNoHandlersAdded() throws Exception {
        TelemetryTransformer transformer = new TelemetryTransformer();
        assertNull(transformer.transform(getClass().getClassLoader(), "com/yammer/telemetry/agent/test/SimpleBean", null, null, new byte[0]));
    }

    @Test
    public void testUnmodifiedWhenNotHandled() throws Exception {
        TelemetryTransformer transformer = new TelemetryTransformer();
        transformer.addHandler(new ClassInstrumentationHandler() {
            @Override
            public boolean transformed(CtClass cc, ClassPool pool) {
                return false;
            }
        });

        assertNull(transformer.transform(getClass().getClassLoader(), "com/yammer/telemetry/agent/test/SimpleBean", null, null, new byte[0]));
    }

    @Test
    public void testModifiedWhenHandled() throws Exception {
        TelemetryTransformer transformer = new TelemetryTransformer();
        transformer.addHandler(new ClassInstrumentationHandler() {
            @Override
            public boolean transformed(CtClass cc, ClassPool pool) {
                return true;
            }
        });

        // Note we didn't actually change anything but claimed we did so expect input & output bytes to match
        assertArrayEquals(new byte[0], transformer.transform(getClass().getClassLoader(), "com/yammer/telemetry/agent/test/SimpleBean", null, null, new byte[0]));
    }

    @Test
    public void testModificationViaClassLoader() throws Exception {
        TelemetryTransformer transformer = new TelemetryTransformer();
        transformer.addHandler(new ClassInstrumentationHandler() {
            @Override
            public boolean transformed(CtClass cc, ClassPool pool) {
                //noinspection SimplifiableIfStatement
                if (!"com.yammer.telemetry.agent.test.SimpleBean".equals(cc.getName())) return false;

                return wrapMethod(cc, "getValue", "{ return $proceed() + \"bar\"; }");
            }
        });

        // Ugh we need to do this via reflection to avoid loading the class first..
        TransformingClassLoader loader = new TransformingClassLoader(transformer);
        Class<?> aClass = loader.loadClass("com.yammer.telemetry.agent.test.SimpleBean");

        String value = "foo";
        Object bean = aClass.newInstance();
        aClass.getDeclaredMethod("setValue", String.class).invoke(bean, value);
        Object result = aClass.getDeclaredMethod("getValue").invoke(bean);

        assertEquals(String.format("%sbar", value), result);
    }

    public static class ClassUtils {
        public static boolean wrapMethod(CtClass cc, String method, String body) {
            try {
                if (cc.isFrozen()) cc.defrost();

                CtMethod getMethod = cc.getDeclaredMethod(method);
                CtMethod copiedMethod = CtNewMethod.copy(getMethod, cc.makeUniqueName(method), cc, null);

                copiedMethod.setModifiers(Modifier.PRIVATE);
                cc.addMethod(copiedMethod);

                getMethod.setBody(body, "this", copiedMethod.getName());

                return true;
            } catch (NotFoundException | CannotCompileException e) {
                throw new RuntimeException(e);
            }
        }
    }
}