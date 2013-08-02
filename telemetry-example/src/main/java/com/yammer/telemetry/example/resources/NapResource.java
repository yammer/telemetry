package com.yammer.telemetry.example.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/nap/{duration}")
@Produces(MediaType.TEXT_PLAIN)
public class NapResource {
    @GET
    public String nap(@PathParam("duration") DurationParam duration) throws InterruptedException {
        Thread.sleep(duration.getDuration().toMilliseconds());
        return "Napped for " + duration.getDuration().toSeconds();
    }
}
