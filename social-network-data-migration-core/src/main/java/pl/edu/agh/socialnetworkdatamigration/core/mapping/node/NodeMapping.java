package pl.edu.agh.socialnetworkdatamigration.core.mapping.node;

import lombok.Getter;

import java.util.List;
import java.util.Objects;

public abstract class NodeMapping {

    @Getter
    private final String nodeLabel;
    @Getter
    private final List<String> identifyingFields;

    protected NodeMapping(String nodeLabel, List<String> identifyingFields) {
        this.nodeLabel = nodeLabel;
        this.identifyingFields = identifyingFields;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NodeMapping that = (NodeMapping) o;
        return getNodeLabel().equals(that.getNodeLabel()) && getIdentifyingFields().equals(that.getIdentifyingFields());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getNodeLabel(), getIdentifyingFields());
    }

    @Override
    public String toString() {
        return String.format("Node label:        %s\nIdentifying fields:        %s", nodeLabel, identifyingFields);
    }
}
