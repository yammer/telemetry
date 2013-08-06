package com.yammer.telemetry.service.models;

public class GefxEdge {
    private String sourceID;
    private String targetID;

    private GefxEdge() { }

    public GefxEdge(String sourceID, String targetID) {
        this.sourceID = sourceID;
        this.targetID = targetID;
    }

    public String getSourceID() {
        return sourceID;
    }

    public String getTargetID() {
        return targetID;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        GefxEdge gefxEdge = (GefxEdge) o;

        if (sourceID != null ? !sourceID.equals(gefxEdge.sourceID) : gefxEdge.sourceID != null) return false;
        if (targetID != null ? !targetID.equals(gefxEdge.targetID) : gefxEdge.targetID != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sourceID != null ? sourceID.hashCode() : 0;
        result = 31 * result + (targetID != null ? targetID.hashCode() : 0);
        return result;
    }
}
