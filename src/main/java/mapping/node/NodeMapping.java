package mapping.node;

import java.util.Objects;

public abstract class NodeMapping {
    private final String nodeLabel;

    protected NodeMapping(String nodeLabel) {
        this.nodeLabel = nodeLabel;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeMapping that = (NodeMapping) o;
        return getNodeLabel().equals(that.getNodeLabel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNodeLabel());
    }

    @Override
    public String toString() {
        return "Node label:        %s\n".formatted(nodeLabel);
    }
}
