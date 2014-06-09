package com.yammer.telemetry.agent.handlers;

import com.yammer.telemetry.agent.jdbc.JdbcDriverClassHandler;
import com.yammer.telemetry.instrumentation.ClassInstrumentationHandler;
import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;

import java.io.IOException;
import java.util.logging.Logger;

public abstract class SubTypeInstrumentationHandler implements ClassInstrumentationHandler {
    private static final Logger LOGGER = Logger.getLogger(SubTypeInstrumentationHandler.class.getName());
    protected final String superTypeName;
    private boolean enabled = true;

    public SubTypeInstrumentationHandler(String superTypeName) {
        this.superTypeName = superTypeName;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public final boolean transformed(CtClass cc, ClassPool pool) {
        try {
            if (canTransform(cc, pool)) {
                return transform(cc, pool);
            }
        } catch (NotFoundException | CannotCompileException | IOException e) {
            // Disable the handler for the remainder.
            LOGGER.warning("Error instrumenting " + cc.getName() + ": " + e.toString() + " [" + getClass().getName() + "]");
            enabled = false;
        }

        return false;
    }

    private boolean canTransform(CtClass cc, ClassPool pool) throws NotFoundException {
        return isEnabled() && cc.subtypeOf(pool.get(superTypeName));
    }

    protected abstract boolean transform(CtClass cc, ClassPool pool) throws NotFoundException, CannotCompileException, IOException;
}
