package mapping;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import mapping.edge.SQLEdgeMapping;
import mapping.node.NodeMapping;
import mapping.node.SQLNodeMapping;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

public class SQLSchemaMapping extends SchemaMapping {
    @Getter @SerializedName("nodes") protected final Set<SQLNodeMapping> nodeMappings = new HashSet<>();
    @Getter @SerializedName("edges") protected final Set<SQLEdgeMapping> edgeMappings = new HashSet<>();

    public void addNodeMapping(SQLNodeMapping nodeMapping){
        nodeMappings.add(nodeMapping);
    }

    public void addEdgeMapping(SQLEdgeMapping edgeMapping){
        edgeMappings.add(edgeMapping);
    }

    public Optional<String> getNodeLabelForTableName(String tableName) {
        return this.getNodeMappings()
                .stream()
                .filter(n -> n.getSqlTableName().equals(tableName))
                .map(NodeMapping::getNodeLabel)
                .findFirst();
    }
}
