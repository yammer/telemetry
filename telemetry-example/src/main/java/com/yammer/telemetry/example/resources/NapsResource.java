package com.yammer.telemetry.example.resources;

import com.yammer.dropwizard.hibernate.UnitOfWork;
import com.yammer.telemetry.example.core.Nap;
import com.yammer.telemetry.example.db.NapDAO;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;

@Path("/naps")
@Produces(MediaType.APPLICATION_JSON)
public class NapsResource {
    private final NapDAO napDAO;

    public NapsResource(NapDAO napDAO) {
        this.napDAO = napDAO;
    }

    @GET
    @UnitOfWork
    public List<Nap> getNaps() {
        return napDAO.findAll();
    }
}
