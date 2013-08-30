package com.yammer.telemetry.example;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.client.JerseyClientConfiguration;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.db.DatabaseConfiguration;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class ExampleConfiguration extends Configuration {
    @Valid
    @NotNull
    @JsonProperty
    private DatabaseConfiguration database = new DatabaseConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    private JerseyClientConfiguration proxyClient = new JerseyClientConfiguration();

    public DatabaseConfiguration getDatabaseConfiguration() {
        return database;
    }

    public JerseyClientConfiguration getProxyClient() {
        return proxyClient;
    }
}
