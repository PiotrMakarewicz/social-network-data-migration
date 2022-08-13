package mapping.edge;

import lombok.Getter;
import lombok.Setter;

import java.util.Objects;

public abstract class EdgeMapping {

    @Getter
    private final String edgeLabel;

    @Setter @Getter
    private String fromNode;

    @Setter @Getter
    private String toNode;

    protected EdgeMapping(String edgeLabel, String fromNode, String toNode) {
        this.edgeLabel = edgeLabel;
        this.fromNode = fromNode;
        this.toNode = toNode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EdgeMapping that = (EdgeMapping) o;
        return getEdgeLabel().equals(that.getEdgeLabel()) && getFromNode().equals(that.getFromNode()) && getToNode().equals(that.getToNode());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getEdgeLabel(), getFromNode(), getToNode());
    }

    @Override
    public String toString() {
        return """
               Edge label:        %s
               Source node:       %s
               Destination node:  %s
               """.formatted(edgeLabel, fromNode, toNode);
    }
}
