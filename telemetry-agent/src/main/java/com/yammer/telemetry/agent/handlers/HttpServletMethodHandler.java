package com.yammer.telemetry.agent.handlers;

import com.google.common.io.Resources;
import javassist.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class HttpServletMethodHandler implements MethodInstrumentationHandler {
    private static final Logger LOGGER = Logger.getLogger(HttpServletMethodHandler.class.getName());
    private boolean enabled = true;

    @Override
    public boolean transformed(CtClass cc, ClassPool pool) {
        if (!enabled) {
            // Handler was disabled earlier for reasons.
            return false;
        }

        try {
            if (cc.subtypeOf(pool.get("javax.servlet.http.HttpServlet"))) {
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
                pool.importPackage("com.yammer.telemetry.agent");
                pool.importPackage("com.yammer.telemetry.agent.handlers");
                pool.importPackage("com.yammer.telemetry.tracing");
                serviceMethod.setBody(source, "this", copiedServiceMethod.getName());

                return true;
            }
        } catch (NotFoundException | CannotCompileException | IOException e) {
            // Can't find the servlet classes…maybe this isn't a web application.
            // Or maybe we couldn't copy the method or compile some expressions, which is un-awesome.
            // Or maybe we couldn't load the source template from the JAR file. This is also un-awesome.
            // Disable the handler for the remainder.
            LOGGER.warning("Error instrumenting HttpServlet: " + e.toString());
            enabled = false;
        }

        return false;
    }
}
