package com.yammer.telemetry.example.db;

import com.google.common.base.Optional;
import com.yammer.dropwizard.hibernate.AbstractDAO;
import com.yammer.telemetry.example.core.Nap;
import org.hibernate.SessionFactory;

import java.util.List;

public class NapDAO extends AbstractDAO<Nap> {
    public NapDAO(SessionFactory sessionFactory) {
        super(sessionFactory);
    }

    public Optional<Nap> findById(long id) {
        return Optional.fromNullable(get(id));
    }

    public Nap create(Nap nap) {
        return persist(nap);
    }

    public List<Nap> findAll() {
        return list(namedQuery("com.yammer.telemetry.example.core.Nap.findAll"));
    }
}
