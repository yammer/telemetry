package com.yammer.telemetry.sinks;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.telemetry.tracing.AnnotationData;
import com.yammer.telemetry.tracing.SpanData;
import com.yammer.telemetry.tracing.SpanSink;
import com.sun.jersey.api.client.Client;

import javax.ws.rs.core.MediaType;

public class TelemetryServiceSpanSink implements SpanSink {
    private final AsyncWebResource spansResource;

    public TelemetryServiceSpanSink(String host, int port) {
        final DefaultApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule());
        config.getSingletons().add(new JacksonMessageBodyProvider(objectMapper, new Validator()));
        final Client client = Client.create(config);
        this.spansResource = client.asyncResource("http://" + host + ":" + port + "/spans");
    }

    @Override
    public void record(SpanData spanData) {
        try {
            spansResource.type(MediaType.APPLICATION_JSON).post(spanData);
        } catch (Exception e) {
            System.err.println("Failed to log span to telemetry service: " + e.toString());
            e.printStackTrace(System.err);
        }
    }

    @Override
    public void recordAnnotation(long traceId, long spanId, AnnotationData annotationData) {
        spansResource.path(Long.toString(traceId)).path(Long.toString(spanId)).type(MediaType.APPLICATION_JSON).post(annotationData);
    }
}
