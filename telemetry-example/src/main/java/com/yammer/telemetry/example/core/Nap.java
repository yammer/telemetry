package com.yammer.telemetry.example.core;

import javax.persistence.*;

@Entity
@Table(name = "naps")
@NamedQueries({
    @NamedQuery(
        name = "com.yammer.telemetry.example.core.Nap.findAll",
        query = "SELECT n FROM Nap n"
    ),
    @NamedQuery(
        name = "com.yammer.telemetry.example.core.Nap.findById",
        query = "SELECT n FROM Nap n WHERE n.id = :id"
    )
})
public class Nap {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(name = "start", nullable = false)
    private long start;

    @Column(name = "duration", nullable = false)
    private long duration;

    public Nap() {
    }

    public Nap(long start, long duration) {
        this.start = start;
        this.duration = duration;
    }

    public long getStart() {
        return start;
    }

    public void setStart(long start) {
        this.start = start;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Nap{" +
                "id=" + id +
                ", start=" + start +
                ", duration=" + duration +
                '}';
    }
}
