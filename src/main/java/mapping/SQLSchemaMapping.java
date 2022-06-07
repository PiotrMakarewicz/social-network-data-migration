package mapping;

import mapping.node.SQLNodeMapping;

import java.util.Optional;

public class SQLSchemaMapping extends SchemaMapping {
    public Optional<String> getNodeLabelForTableName(String tableName) {
        return this.getNodeMappings()
                .stream()
                .filter(n -> ((SQLNodeMapping)n).getSqlTableName().equals(tableName))
                .map(n -> n.getNodeLabel())
                .findFirst();
    }
}
