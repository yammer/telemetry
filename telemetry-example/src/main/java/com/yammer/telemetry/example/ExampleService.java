package com.yammer.telemetry.example;

import com.yammer.telemetry.example.resources.NapResource;
import com.yammer.telemetry.example.resources.ProxyResource;
import com.sun.jersey.api.client.Client;
import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.client.JerseyClientBuilder;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

public class ExampleService extends Service<ExampleConfiguration> {
    public static void main(String[] args) throws Exception {
        new ExampleService().run(args);
    }

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.setName("example");
    }

    @Override
    public void run(ExampleConfiguration configuration, Environment environment) throws Exception {
        Client client = new JerseyClientBuilder().using(environment).build();

        environment.addResource(new ProxyResource(client));
        environment.addResource(new NapResource());
    }
}
