package com.hypnoticocelot.telemetry.example.resources;

import com.sun.jersey.api.client.Client;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

@Path("/proxy/to/{hostname}/{uri:.*}")
@Produces(MediaType.TEXT_PLAIN)
public class ProxyResource {
    private final Client client;

    public ProxyResource(Client client) {
        this.client = client;
    }

    @GET
    public String proxyTo(@PathParam("hostname") String hostname, @PathParam("uri") String uri, @Context UriInfo uriInfo) {
        return client.resource("http://" + hostname + "/" + uri).queryParams(uriInfo.getQueryParameters()).get(String.class);
    }
}
