package com.yammer.telemetry.service.models;

import java.util.Set;

public class GefxData {
    private Set<GefxNode> nodes;
    private Set<GefxEdge> edges;

    private GefxData() { }

    public GefxData(Set<GefxNode> nodes, Set<GefxEdge> edges) {
        this.nodes = nodes;
        this.edges = edges;
    }

    public Set<GefxNode> getNodes() {
        return nodes;
    }

    public Set<GefxEdge> getEdges() {
        return edges;
    }
}
