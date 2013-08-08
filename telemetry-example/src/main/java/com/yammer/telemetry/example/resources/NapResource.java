package com.yammer.telemetry.example.resources;

import com.yammer.dropwizard.hibernate.UnitOfWork;
import com.yammer.telemetry.example.core.Nap;
import com.yammer.telemetry.example.db.NapDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/nap/{duration}")
@Produces(MediaType.TEXT_PLAIN)
public class NapResource {
    private final NapDAO napDAO;

    public NapResource(NapDAO napDAO) {
        this.napDAO = napDAO;
    }

    @GET
    @UnitOfWork
    public String nap(@PathParam("duration") DurationParam duration) throws InterruptedException {
        final Nap nap = new Nap(System.currentTimeMillis(), duration.getDuration().toMilliseconds());
        Thread.sleep(duration.getDuration().toMilliseconds());
        napDAO.create(nap);
        return "Napped for " + duration.getDuration().toSeconds();
    }
}
