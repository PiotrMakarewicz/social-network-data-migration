package mapping;

import mapping.edge.EdgeMapping;
import mapping.node.NodeMapping;

import java.util.Collection;
import java.util.HashSet;

public class SchemaMapping {
    private final Collection<NodeMapping> nodeMappings = new HashSet<>();
    private final Collection<EdgeMapping> edgeMappings = new HashSet<>();

    public SchemaMapping(){}

    public void addNodeMapping(NodeMapping nodeMapping){
        nodeMappings.add(nodeMapping);
    }

    public void addEdgeMapping(EdgeMapping edgeMapping){
        edgeMappings.add(edgeMapping);
    }

    public Collection<NodeMapping> getNodeMappings() {
        return nodeMappings;
    }

    public Collection<EdgeMapping> getEdgeMappings() {
        return edgeMappings;
    }
}
