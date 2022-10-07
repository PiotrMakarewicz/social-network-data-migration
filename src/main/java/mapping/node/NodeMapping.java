package mapping.node;

import lombok.Getter;

import java.util.Objects;

public abstract class NodeMapping {

    @Getter
    private final String nodeLabel;

    protected NodeMapping(String nodeLabel) {
        this.nodeLabel = nodeLabel;
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
        return String.format("Node label:        %s\n", nodeLabel);
    }
}
