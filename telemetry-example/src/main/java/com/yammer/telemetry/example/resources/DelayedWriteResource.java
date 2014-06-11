package com.yammer.telemetry.example.resources;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.annotation.Timed;
import com.yammer.metrics.core.Timer;
import com.yammer.metrics.core.TimerContext;
import com.yammer.telemetry.example.core.Nap;
import com.yammer.telemetry.example.db.NapDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Path("/delayed")
public class DelayedWriteResource {
    private final NapDAO napDAO;
    private final ScheduledExecutorService executorService;
    private static final Timer timer = Metrics.newTimer(DelayedWriteResource.class, "delayed-write");

    public DelayedWriteResource(NapDAO napDAO, ScheduledExecutorService executorService) {
        this.napDAO = napDAO;
        this.executorService = executorService;
    }

    @GET
    @Timed
    @Path("/start/{start}/duration/{duration}")
    public String delayedWrite(@PathParam("start") long start, @PathParam("duration") long duration) {
        final Nap nap = new Nap(start, duration);
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                TimerContext time = timer.time();
                napDAO.create(nap);
                time.stop();
            }
        }, duration, TimeUnit.MILLISECONDS);
        return "Submitted: " + nap;
    }

}
