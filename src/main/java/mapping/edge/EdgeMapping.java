package mapping.edge;

public abstract class EdgeMapping {
    private final String edgeLabel;
    private final String fromNode;
    private final String toNode;

    protected EdgeMapping(String edgeLabel, String fromNode, String toNode) {
        this.edgeLabel = edgeLabel;
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    public String getEdgeLabel() {
        return edgeLabel;
    }
}
