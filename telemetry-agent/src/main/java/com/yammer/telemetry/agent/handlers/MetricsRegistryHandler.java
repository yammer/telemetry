package com.yammer.telemetry.agent.handlers;

import javassist.*;

import java.io.IOException;

public class MetricsRegistryHandler extends SubTypeInstrumentationHandler {
    public MetricsRegistryHandler() {
        super("com.yammer.metrics.core.MetricsRegistry");
    }

    @Override
    protected boolean transform(CtClass cc, ClassPool pool) throws NotFoundException, CannotCompileException, IOException {
        if (!superTypeName.equals(cc.getName())) {
            return false;
        }

        switchImplementation(cc, pool, "com.yammer.metrics.core.Timer", "com.yammer.metrics.core.InstrumentedTimer");
        switchImplementation(cc, pool, "com.yammer.metrics.core.Meter", "com.yammer.metrics.core.InstrumentedMeter");

        CtMethod getOrAdd = cc.getDeclaredMethod("getOrAdd");
        getOrAdd.insertBefore(
                "{" +
                        "  if ($2 instanceof com.yammer.metrics.core.MetricNameAware) {" +
                        "    ((com.yammer.metrics.core.MetricNameAware)$2).setMetricName((com.yammer.metrics.core.MetricName)$1);" +
                        "  }" +
                        "}");

        cc.debugWriteFile("/Users/idavies/debug/" + System.nanoTime());

        return true;
    }

    private void switchImplementation(CtClass cc, ClassPool pool, String from, String to) throws NotFoundException, CannotCompileException {
        CtClass oldClass = pool.get(from);
        CtClass newClass = pool.get(to);

        CodeConverter converter = new CodeConverter();
        converter.replaceNew(oldClass, newClass);

        cc.instrument(converter);
    }

//    @Override
//    public boolean transformed(CtClass cc, ClassPool pool) {
//        try {
//            if ("com.yammer.metrics.core.Timer".equals(cc.getName())) {
//                return addNamePropertyToMetricClass(cc, pool);
//            }
//            if ("com.yammer.metrics.core.MetricsRegistry".equals(cc.getName())) {
//                CtMethod getOrAdd = cc.getDeclaredMethod("getOrAdd");
//                getOrAdd.insertBefore("System.out.println($1);");
////                getOrAdd.insertBefore("{ System.out.println(\"Got: \" + java.util.Arrays.toString(new Object[] {$$})); }");
//                getOrAdd.insertAfter("");
//                return true;
//            }
//            return false;
//        } catch (CannotCompileException e) {
//            e.printStackTrace();
//            return false;
//        } catch (NotFoundException e) {
//            e.printStackTrace();
//            return false;
//        }
//    }
//
//    private boolean addNamePropertyToMetricClass(CtClass cc, ClassPool pool) throws CannotCompileException {
//        System.out.println("Adding to: " + cc);
//        CtField ctField = CtField.make("private com.yammer.metrics.core.MetricName metricName;", cc);
//        cc.addField(ctField);
//        CtMethod getMetricName = CtNewMethod.getter("getMetricName", ctField);
//        CtMethod setMetricName = CtNewMethod.setter("setMetricName", ctField);
//        cc.addMethod(getMetricName);
//        cc.addMethod(setMetricName);
//        return true;
//    }
}
