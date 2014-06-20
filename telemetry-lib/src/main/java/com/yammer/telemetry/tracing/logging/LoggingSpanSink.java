package com.yammer.telemetry.tracing.logging;

import com.yammer.telemetry.tracing.AsynchronousSpanSink;

import java.io.IOException;

public class LoggingSpanSink extends AsynchronousSpanSink {
    public LoggingSpanSink(String file) throws IOException {
        super(LogJobFactory.withFile(file));
    }
}
