package mapping;

import mapping.edge.SQLEdgeMapping;
import mapping.node.SQLNodeMapping;

import java.util.Optional;

public class SQLSchemaMapping extends MultiSchemaMapping<SQLNodeMapping, SQLEdgeMapping> {

    public Optional<String> getNodeLabelForTableName(String tableName) {
        return this.getNodeMappings()
                .stream()
                .filter(n -> n.getSqlTableName().equals(tableName))
                .map(n -> n.getNodeLabel())
                .findFirst();
    }
}
