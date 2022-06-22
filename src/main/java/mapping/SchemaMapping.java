package mapping;

import mapping.edge.EdgeMapping;
import mapping.node.NodeMapping;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public abstract class SchemaMapping {
    private final Set<NodeMapping> nodeMappings = new HashSet<>();
    private final Set<EdgeMapping> edgeMappings = new HashSet<>();

    public SchemaMapping(){}

    public void addNodeMapping(NodeMapping nodeMapping){
        nodeMappings.add(nodeMapping);
    }

    public void addEdgeMapping(EdgeMapping edgeMapping){
        edgeMappings.add(edgeMapping);
    }

    public Set<NodeMapping> getNodeMappings() {
        return nodeMappings;
    }

    public Set<EdgeMapping> getEdgeMappings() {
        return edgeMappings;
    }

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
