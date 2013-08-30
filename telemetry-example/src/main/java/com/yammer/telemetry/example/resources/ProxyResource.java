package com.yammer.telemetry.example.resources;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

@Path("/proxy/to/{hostname}/{uri:.*}")
public class ProxyResource {
    private final Client client;

    public ProxyResource(Client client) {
        this.client = client;
    }

    @GET
    public Response proxyTo(@PathParam("hostname") String hostname, @PathParam("uri") String uri, @Context UriInfo uriInfo) {
        final ClientResponse response = client.resource("http://" + hostname + "/" + uri)
                                              .queryParams(uriInfo.getQueryParameters())
                                              .get(ClientResponse.class);
        return Response.ok(response.getEntityInputStream()).type(response.getType()).build();
    }
}
