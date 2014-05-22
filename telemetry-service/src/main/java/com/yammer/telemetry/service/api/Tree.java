package com.yammer.telemetry.service.api;

import java.math.BigInteger;

public class Tree {
    private final BigInteger id;
    private final TreeSpan root;

    public Tree(BigInteger id, TreeSpan root) {
        this.id = id;
        this.root = root;
    }

    public BigInteger getId() {
        return id;
    }

    public TreeSpan getRoot() {
        return root;
    }
}
