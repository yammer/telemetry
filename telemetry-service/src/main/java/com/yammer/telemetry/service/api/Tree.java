package com.yammer.telemetry.service.api;

public class Tree {
    private final long id;
    private final TreeSpan root;

    public Tree(long id, TreeSpan root) {
        this.id = id;
        this.root = root;
    }

    public long getId() {
        return id;
    }

    public TreeSpan getRoot() {
        return root;
    }
}
