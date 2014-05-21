package com.yammer.telemetry.agent.jdbc;

import com.yammer.telemetry.agent.handlers.SubTypeInstrumentationHandler;
import javassist.*;

import java.io.IOException;

public class JdbcDriverClassHandler extends SubTypeInstrumentationHandler {
    public JdbcDriverClassHandler() {
        super("java.sql.Driver");
    }

    @Override
    protected boolean transform(CtClass cc, ClassPool pool) throws NotFoundException, CannotCompileException, IOException {
        CtMethod connect;
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
        copiedConnect.setModifiers(Modifier.PRIVATE);
        cc.addMethod(copiedConnect);

        pool.importPackage("com.yammer.telemetry.agent.jdbc");
        connect.setBody("{ return new InstrumentedConnection($1, $proceed($$)); }", "this", copiedConnect.getName());

        return true;
    }
}
