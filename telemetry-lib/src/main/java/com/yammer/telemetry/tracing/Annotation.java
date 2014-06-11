package com.yammer.telemetry.tracing;

public class Annotation implements AnnotationData {
    private long startTimeNanos;
    private String name;
    private String message;

    public Annotation(long startTimeNanos, String name) {
        this(startTimeNanos, name, null);
    }

    public Annotation(long startTimeNanos, String name, String message) {
        this.startTimeNanos = startTimeNanos;
        this.name = name;
        this.message = message;
    }

    @Override
    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "Annotation{" +
                "startTimeNanos=" + startTimeNanos +
                ", name='" + name + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
