package pl.edu.agh.socialnetworkdatamigration.core.mapping;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.SQLEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.SQLNodeMapping;

import java.util.Optional;

public class SQLSchemaMapping extends MultiSchemaMapping<SQLNodeMapping, SQLEdgeMapping> {

    public Optional<String> getNodeLabelForTableName(String tableName) {
        return getNodeForTable(tableName).map(SQLNodeMapping::getNodeLabel);
    }

    public Optional<SQLNodeMapping> getNodeForTable(String table) {
        return this.getNodeMappings()
                .stream()
                .filter(n -> n.getSqlTableName().equals(table))
                .findFirst();
    }
}
