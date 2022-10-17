package pl.edu.agh.socialnetworkdatamigration.core.mapping;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.SQLNodeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.SQLEdgeMapping;

import java.util.Optional;

public class SQLSchemaMapping extends MultiSchemaMapping<SQLNodeMapping, SQLEdgeMapping> {
    public SQLSchemaMapping(){}

    public Optional<String> getNodeLabelForTableName(String tableName) {
        return this.getNodeMappings()
                .stream()
                .filter(n -> n.getSqlTableName().equals(tableName))
                .map(n -> n.getNodeLabel())
                .findFirst();
    }
}
