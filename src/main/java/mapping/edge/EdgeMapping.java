package mapping.edge;

public abstract class EdgeMapping {
    private final String edgeLabel;

    protected EdgeMapping(String edgeLabel) {
        this.edgeLabel = edgeLabel;
    }

    public String getEdgeLabel() {
        return edgeLabel;
    }
}
