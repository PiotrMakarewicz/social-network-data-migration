package mapping.node;

public abstract class NodeMapping {
    private final String nodeLabel;

    protected NodeMapping(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }
}
