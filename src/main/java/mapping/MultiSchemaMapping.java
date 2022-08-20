package mapping;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import mapping.edge.EdgeMapping;
import mapping.node.NodeMapping;

import java.util.HashSet;
import java.util.Set;

// klasa będzie też dziedziczona przez XMLSchemaMapping, dlatego jest osobną klasą niż SQLSchemaMapping
public abstract class MultiSchemaMapping<N extends NodeMapping, E extends EdgeMapping> extends SchemaMapping<N, E> {

    @Getter @SerializedName("nodes")
    protected final Set<N> nodeMappings = new HashSet<>();

    @Getter @SerializedName("edges")
    protected final Set<E> edgeMappings = new HashSet<>();

    public void addNodeMapping(N nodeMapping){
        nodeMappings.add(nodeMapping);
    }

    public void addEdgeMapping(E edgeMapping){
        edgeMappings.add(edgeMapping);
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
