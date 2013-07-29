package com.yammer.telemetry.sinks;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.yammer.telemetry.tracing.SpanData;
import com.yammer.telemetry.tracing.SpanSink;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;

import javax.ws.rs.core.MediaType;
import java.util.logging.Logger;

public class TelemetryServiceSpanSink implements SpanSink {
    private final Logger LOG = Logger.getLogger(TelemetryServiceSpanSink.class.getName());

    private final Client client;
    private final WebResource resource;

    public TelemetryServiceSpanSink(String host, int port) {
        ClientConfig config = new DefaultClientConfig(JacksonJsonProvider.class);
        this.client = Client.create(config);
        this.resource = client.resource("http://" + host + ":" + port + "/spans");
    }

    @Override
    public void record(SpanData spanData) {
        try {
            resource.type(MediaType.APPLICATION_JSON).post(spanData);
        } catch (Exception e) {
            System.err.println("Failed to log span to telemetry service: " + e.toString());
            e.printStackTrace(System.err);
        }
    }
}
