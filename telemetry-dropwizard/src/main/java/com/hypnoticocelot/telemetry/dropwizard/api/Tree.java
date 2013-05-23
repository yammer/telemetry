package com.hypnoticocelot.telemetry.dropwizard.api;

import java.util.UUID;

public class Tree {
    private final UUID id;
    private final TreeSpan root;

    public Tree(UUID id, TreeSpan root) {
        this.id = id;
        this.root = root;
    }

    public UUID getId() {
        return id;
    }

    public TreeSpan getRoot() {
        return root;
    }
}
