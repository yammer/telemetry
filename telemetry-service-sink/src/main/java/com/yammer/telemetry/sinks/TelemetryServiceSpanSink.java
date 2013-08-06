package com.yammer.telemetry.sinks;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.AsyncWebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.yammer.telemetry.tracing.AnnotationData;
import com.yammer.telemetry.tracing.SpanData;
import com.yammer.telemetry.tracing.SpanSink;
import com.sun.jersey.api.client.Client;

import javax.ws.rs.core.MediaType;
import java.util.UUID;

public class TelemetryServiceSpanSink implements SpanSink {
    private final Client client;
    private final AsyncWebResource spansResource;

    public TelemetryServiceSpanSink(String host, int port) {
        ClientConfig config = new DefaultClientConfig(JacksonJsonProvider.class);
        this.client = Client.create(config);
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
    public void recordAnnotation(UUID traceId, UUID spanId, AnnotationData annotation) {
        spansResource.path(traceId.toString()).path(spanId.toString()).type(MediaType.APPLICATION_JSON).post(annotation);
    }
}
