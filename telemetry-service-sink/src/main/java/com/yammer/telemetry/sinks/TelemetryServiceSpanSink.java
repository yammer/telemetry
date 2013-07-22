package com.yammer.telemetry.sinks;

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

    public TelemetryServiceSpanSink() {
        this.client = new Client();
        this.resource = client.resource("http://localhost:9090/spans");
    }

    @Override
    public void record(SpanData spanData) {
        try {
            resource.type(MediaType.APPLICATION_JSON).post(spanData);
        } catch (Exception e) {
            LOG.warning("Failed to log span to telemetry service: " + e.toString());
        }
    }
}
