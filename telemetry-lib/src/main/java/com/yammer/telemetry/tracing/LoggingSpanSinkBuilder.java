package com.yammer.telemetry.tracing;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.ImmutableList;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

public class LoggingSpanSinkBuilder {
    private ExecutorService executor = Executors.newSingleThreadExecutor();
    private WriterProvider writerProvider;

    public AsynchronousSpanSink build() {
        return new AsynchronousSpanSink(executor, new LogJobFactory(writerProvider));
    }

    public LoggingSpanSinkBuilder withWriter(final Writer writer) {
        return usingWriterProvider(new WriterProvider() {
            @Override
            public Writer getWriter() {
                return writer;
            }
        });
    }

    private LoggingSpanSinkBuilder usingWriterProvider(WriterProvider writerProvider) {
        this.writerProvider = writerProvider;
        return this;
    }

    public LoggingSpanSinkBuilder withFile(final String file) throws IOException {
        // We try and open the file for append to get early warning of misconfiguration, it will fail is file is not
        // writable.
        try (FileWriter ignored = new FileWriter(file, true)) {
            return usingWriterProvider(new WriterProvider() {
                @Override
                public Writer getWriter() throws IOException {
                    return new FileWriter(file, true);
                }
            });
        }
    }

    private static class LogJobFactory implements AsynchronousSpanSink.JobFactory {
        private static final Logger LOG = Logger.getLogger(LogJob.class.getName());

        private final WriterProvider writerProvider;
        private ObjectMapper objectMapper;

        public LogJobFactory(WriterProvider writerProvider) {
            this.writerProvider = writerProvider;
            this.objectMapper = new ObjectMapper().registerModule(new GuavaModule());
        }

        @Override
        public Runnable createJob(SpanData data) {
            return genericJob(data);
        }

        @Override
        public Runnable createJob(BigInteger traceId, BigInteger spanId, AnnotationData data) {
            HashMap<String, Object> map = new HashMap<>();
            map.put("traceId", traceId.toString());
            map.put("spanId", spanId.toString());
            map.put("annotations", ImmutableList.of(data));

            return genericJob(map);
        }

        private Runnable genericJob(Object object) {
            return new LogJob(object);
        }

        private class LogJob implements Runnable {
            private final Object object;

            public LogJob(Object object) {
                this.object = object;
            }

            @Override
            public void run() {
                try (PrintWriter writer = new PrintWriter(writerProvider.getWriter())) {
                    writer.println(objectMapper.writeValueAsString(object));
                    writer.flush();
                } catch (IOException e) {
                    LOG.throwing(LogJob.class.getName(), "run", e);
                }
            }
        }
    }

    private interface WriterProvider {
        Writer getWriter() throws IOException;
    }
}
