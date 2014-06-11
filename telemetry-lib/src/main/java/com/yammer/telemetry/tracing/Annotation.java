package com.yammer.telemetry.tracing;

public class Annotation implements AnnotationData {
    private long loggedAt;
    private String name;
    private String message;

    public Annotation(long loggedAt, String name) {
        this(loggedAt, name, null);
    }

    public Annotation(long loggedAt, String name, String message) {
        this.loggedAt = loggedAt;
        this.name = name;
        this.message = message;
    }

    @Override
    public long getLoggedAt() {
        return loggedAt;
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
                "loggedAt=" + loggedAt +
                ", name='" + name + '\'' +
                ", message='" + message + '\'' +
                '}';
    }
}
