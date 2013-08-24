package com.yammer.telemetry.example;

import com.yammer.dropwizard.db.DatabaseConfiguration;
import com.yammer.dropwizard.hibernate.HibernateBundle;
import com.yammer.dropwizard.migrations.MigrationsBundle;
import com.yammer.telemetry.example.core.Nap;
import com.yammer.telemetry.example.db.NapDAO;
import com.yammer.telemetry.example.resources.NapResource;
import com.yammer.telemetry.example.resources.NapsResource;
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

    private HibernateBundle<ExampleConfiguration> hibernate = new HibernateBundle<ExampleConfiguration>(Nap.class) {
        @Override
        public DatabaseConfiguration getDatabaseConfiguration(ExampleConfiguration exampleConfiguration) {
            return exampleConfiguration.getDatabaseConfiguration();
        }
    };

    @Override
    public void initialize(Bootstrap<ExampleConfiguration> bootstrap) {
        bootstrap.setName("example");
        bootstrap.addBundle(hibernate);
        bootstrap.addBundle(new MigrationsBundle<ExampleConfiguration>() {
            @Override
            public DatabaseConfiguration getDatabaseConfiguration(ExampleConfiguration configuration) {
                return configuration.getDatabaseConfiguration();
            }
        });
    }

    @Override
    public void run(ExampleConfiguration configuration, Environment environment) throws Exception {
        Client client = new JerseyClientBuilder().using(environment).build();
        environment.addResource(new ProxyResource(client));

        final NapDAO napDAO = new NapDAO(hibernate.getSessionFactory());
        environment.addResource(new NapResource(napDAO));
        environment.addResource(new NapsResource(napDAO));
    }
}
