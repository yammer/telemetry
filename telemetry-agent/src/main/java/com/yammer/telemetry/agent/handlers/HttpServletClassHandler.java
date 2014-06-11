package com.yammer.telemetry.agent.handlers;

import com.google.common.io.Resources;
import javassist.*;

import java.io.IOException;
import java.nio.charset.Charset;

public class HttpServletClassHandler extends SubTypeInstrumentationHandler {
    public HttpServletClassHandler() {
        super("javax.servlet.http.HttpServlet");
    }

    protected boolean transform(CtClass cc, ClassPool pool) throws NotFoundException, CannotCompileException, IOException {
        // Find the service(HttpServletRequest, HttpServletResponse) method so we can wrap it.
        CtClass request = pool.get("javax.servlet.http.HttpServletRequest");
        CtClass response = pool.get("javax.servlet.http.HttpServletResponse");
        CtMethod serviceMethod;
        try {
            serviceMethod = cc.getDeclaredMethod("service", new CtClass[]{request, response});
        } catch (NotFoundException e) {
            // Not every servlet will implement this method and that's okay.
            return false;
        }

        // Copy the method to a uniquely named location that won't conflict with anything.
        CtMethod copiedServiceMethod = CtNewMethod.copy(serviceMethod, cc.makeUniqueName("service"), cc, null);
        copiedServiceMethod.setModifiers(Modifier.PRIVATE);
        cc.addMethod(copiedServiceMethod);

        // Swap in a new method body for service() that invokes the copied version of service().
        final String source = Resources.toString(Resources.getResource(getClass(), "HttpServlet_service.javassist"), Charset.forName("utf-8"));
        pool.importPackage("java.util");
        pool.importPackage("java.math");
        pool.importPackage("com.yammer.telemetry.agent");
        pool.importPackage("com.yammer.telemetry.agent.handlers");
        pool.importPackage("com.yammer.telemetry.tracing");
        serviceMethod.setBody(source, "this", copiedServiceMethod.getName());

        return true;
    }
}
