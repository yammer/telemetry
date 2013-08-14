package com.yammer.telemetry.tracing;

/**
 * Created with IntelliJ IDEA.
 * User: rkennedy
 * Date: 8/13/13
 * Time: 10:02 PM
 * To change this template use File | Settings | File Templates.
 */
public interface AnnotationData {
    long getStartTimeNanos();

    String getName();

    String getMessage();
}
