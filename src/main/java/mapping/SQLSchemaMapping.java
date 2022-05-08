package mapping;

import mapping.node.SQLNodeMapping;

public class SQLSchemaMapping extends SchemaMapping {
    public String getNodeLabelForTableName(String tableName) {
        return this.getNodeMappings()
                .stream()
                .filter(n -> ((SQLNodeMapping)n).getSqlTableName().equals(tableName))
                .map(n -> n.getNodeLabel())
                .findFirst()
                .get();
    }
}
