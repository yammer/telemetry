package com.yammer.telemetry.tracing;

public class AnnotationData {
    private long startTimeNanos;
    private String message;

    private AnnotationData() { }

    public AnnotationData(long startTimeNanos, String message) {
        this.startTimeNanos = startTimeNanos;
        this.message = message;
    }

    public long getStartTimeNanos() {
        return startTimeNanos;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "AnnotationData{" +
                "startTimeNanos=" + startTimeNanos +
                ", message='" + message + '\'' +
                '}';
    }
}
