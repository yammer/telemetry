package com.yammer.telemetry.tracing.logging;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.yammer.telemetry.tracing.AsynchronousSpanSink;
import com.yammer.telemetry.tracing.SpanData;

import java.io.*;
import java.nio.charset.Charset;
import java.util.logging.Logger;

public class LogJobFactory implements AsynchronousSpanSink.JobFactory {
    private static final Logger LOG = Logger.getLogger(LogJob.class.getName());

    private final WriterProvider writerProvider;
    private ObjectMapper objectMapper;

    private LogJobFactory(WriterProvider writerProvider) {
        this.writerProvider = writerProvider;
        this.objectMapper = new ObjectMapper().setPropertyNamingStrategy(new PropertyNamingStrategy.LowerCaseWithUnderscoresStrategy()).setSerializationInclusion(JsonInclude.Include.NON_NULL).registerModule(new GuavaModule());
    }

    public static LogJobFactory withWriter(final Writer writer) {
        if (writer == null) throw new NullPointerException("Writer must not be null");
        return withWriterProvider(new WriterProvider() {
            @Override
            public Writer getWriter() throws IOException {
                return writer;
            }
        });
    }

    public static LogJobFactory withFile(final String file) throws IOException {
        // We try and open the file for append to get early warning of misconfiguration, it will fail is file is not
        // writable.
        try (FileOutputStream ignored = new FileOutputStream(file, true)) {
            return withWriterProvider(new WriterProvider() {
                @Override
                public Writer getWriter() throws IOException {
                    return new OutputStreamWriter(new FileOutputStream(file, true), Charset.forName("UTF-8").newEncoder());
                }
            });
        }
    }

    @Override
    public Runnable createJob(SpanData data) {
        return new LogJob(data);
    }

    private static LogJobFactory withWriterProvider(WriterProvider writerProvider) {
        return new LogJobFactory(writerProvider);
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

    private static interface WriterProvider {
        Writer getWriter() throws IOException;
    }
}
