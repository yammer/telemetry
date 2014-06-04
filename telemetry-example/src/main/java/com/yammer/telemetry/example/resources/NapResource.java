package com.yammer.telemetry.example.resources;

import com.yammer.dropwizard.hibernate.UnitOfWork;
import com.yammer.metrics.Metrics;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.Counter;
import com.yammer.metrics.core.Meter;
import com.yammer.telemetry.example.core.Nap;
import com.yammer.telemetry.example.db.NapDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.concurrent.TimeUnit;

@Path("/nap/{duration}")
@Produces(MediaType.TEXT_PLAIN)
public class NapResource {
    private final NapDAO napDAO;
    private final Meter sleepsTaken = Metrics.newMeter(NapResource.class, "sleeps", "taken", TimeUnit.MILLISECONDS);
    private final Meter sleepsDuration = Metrics.newMeter(NapResource.class, "sleeps", "duration", TimeUnit.MILLISECONDS);

    public NapResource(NapDAO napDAO) {
        this.napDAO = napDAO;
    }

    @GET
    @UnitOfWork
    @Timed
    public String nap(@PathParam("duration") DurationParam duration) throws InterruptedException {
        final Nap nap = new Nap(System.currentTimeMillis(), duration.getDuration().toMilliseconds());
        sleepsTaken.mark();
        sleepsDuration.mark(duration.getDuration().toMilliseconds());
        Thread.sleep(duration.getDuration().toMilliseconds());
        napDAO.create(nap);
        return "Napped for " + duration.getDuration().toSeconds();
    }
}
