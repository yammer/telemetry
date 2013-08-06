package com.yammer.telemetry.service.models;

public class GefxNode {
    private String id;
    private String label;

    private GefxNode() { }

    public GefxNode(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GefxNode gefxNode = (GefxNode) o;

        if (id != null ? !id.equals(gefxNode.id) : gefxNode.id != null) return false;
        if (label != null ? !label.equals(gefxNode.label) : gefxNode.label != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (label != null ? label.hashCode() : 0);
        return result;
    }
}
