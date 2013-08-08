package com.yammer.telemetry.agent.jdbc;

import com.yammer.telemetry.agent.handlers.ClassInstrumentationHandler;
import javassist.*;

import java.util.logging.Logger;

public class JdbcDriverClassHandler implements ClassInstrumentationHandler {
    private static final Logger LOGGER = Logger.getLogger(JdbcDriverClassHandler.class.getName());
    private boolean enabled = true;

    @Override
    public boolean transformed(CtClass cc, ClassPool pool) {
        if (!enabled) {
            // Handler was disabled earlier for reasons.
            return false;
        }

        try {
            if (cc.subtypeOf(pool.get("java.sql.Driver"))) {
                CtMethod connect = null;
                try {
                    connect = cc.getDeclaredMethod("connect", new CtClass[]{pool.get("java.lang.String"), pool.get("java.util.Properties")});
                    if (Modifier.isAbstract(connect.getModifiers())) {
                        // Don't modify abstract connect() methods.
                        return false;
                    }
                } catch (NotFoundException e) {
                    // This is okay, it just means this particular driver doesn't have a connect(String, Properties).
                    return false;
                }

                // Copy the method to a uniquely named location that won't conflict with anything.
                CtMethod copiedConnect = CtNewMethod.copy(connect, cc.makeUniqueName("connect"), cc, null);
                copiedConnect.setModifiers(javassist.Modifier.PRIVATE);
                cc.addMethod(copiedConnect);

                pool.importPackage("com.yammer.telemetry.agent.jdbc");
                connect.setBody("{ return new InstrumentedConnection($1, $proceed($$)); }", "this", copiedConnect.getName());

                return true;
            }
        } catch (NotFoundException | CannotCompileException  e) {
            // Disable the handler for the remainder.
            LOGGER.warning("Error instrumenting Apache HttpClient: " + e.toString());
            enabled = false;
        }

        return false;
    }
}
