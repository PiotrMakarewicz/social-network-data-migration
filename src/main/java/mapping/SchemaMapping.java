package mapping;

import mapping.edge.EdgeMapping;
import mapping.node.NodeMapping;

import java.util.HashSet;
import java.util.Set;

public abstract class SchemaMapping {
    protected final Set<NodeMapping> nodeMappings = new HashSet<>();
    protected final Set<EdgeMapping> edgeMappings = new HashSet<>();

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("Node mappings:\n");
        nodeMappings.forEach(nodeMapping -> builder.append(nodeMapping.toString()).append("\n"));
        builder.append("\nEdge mappings:\n");
        edgeMappings.forEach(edgeMapping -> builder.append(edgeMapping.toString()).append("\n"));
        return builder.toString();
    }
}
