package com.yammer.telemetry.tracing;

public class AnnotationData {
    private long startTimeNanos;
    private String name;
    private String message;

    private AnnotationData() { }

    public AnnotationData(long startTimeNanos, String name) {
        this(startTimeNanos, name, null);
    }

    public AnnotationData(long startTimeNanos, String name, String message) {
        this.startTimeNanos = startTimeNanos;
        this.name = name;
        this.message = message;
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    public String getName() {
        return name;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "AnnotationData{" +
                "startTimeNanos=" + startTimeNanos +
                ", name='" + name + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
