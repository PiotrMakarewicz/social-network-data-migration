package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.ForeignKeyMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.JoinTableMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.SQLEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.SQLNodeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.utils.SchemaMetaData;
import pl.edu.agh.socialnetworkdatamigration.core.utils.info.ColumnInfo;
import pl.edu.agh.socialnetworkdatamigration.core.utils.info.ForeignKeyInfo;

import java.util.List;
import java.util.stream.Collectors;

public class PostgresAddingStrategy extends PostgresStrategy {
    public PostgresAddingStrategy(SchemaMetaData schemaMetaData) {
        super(schemaMetaData);
    }

    @Override
    protected String createIndexQuery(SQLNodeMapping nodeMapping) {
        String nodeLabel = nodeMapping.getNodeLabel();
        String tableName = nodeMapping.getSqlTableName();
        List<String> primaryKeyColumns = schemaMetaData
                .getPrimaryKeyColumns(tableName)
                .stream()
                .map(column -> column.columnName)
                .collect(Collectors.toList());

        String call = String.format("CREATE INDEX %s IF NOT EXISTS FOR (n:%s) ON (",
                (nodeLabel + "_index"),
                nodeLabel);
        StringBuilder callBuilder = new StringBuilder(call);
        primaryKeyColumns.forEach(column -> {
            callBuilder.append(String.format("n.__%s,", column));
        });
        callBuilder.append("n.__table_name)");
        return callBuilder.toString();
    }

    @Override
    protected String createNodeQuery(SQLNodeMapping nodeMapping) {
        String tableName = nodeMapping.getSqlTableName();

        String loadJdbcCall = String.format("CALL apoc.load.jdbc(\"jdbc:postgresql://%s/%s?user=%s&password=%s\",\"%s\") YIELD row",
                schemaMetaData.getPostgresHost(),
                schemaMetaData.getPostgresDB(),
                schemaMetaData.getPostgresUser(),
                schemaMetaData.getPostgresPassword(),
                tableName);

        StringBuilder mappedColumns = new StringBuilder();
        mappedColumns.append(String.format("__table_name:'%s',", tableName));
        nodeMapping.getMappedColumns().forEach((column, attribute) -> {
            mappedColumns.append(String.format("%s:coalesce(row.%s, 'NULL'),", attribute, column)); // null values
            // represented as 'NULL'
        });
        mappedColumns.setLength(mappedColumns.length() - 1); // delete trailing comma

        StringBuilder primaryKeyColumns = new StringBuilder();
        schemaMetaData.getPrimaryKeyColumns(tableName).forEach(column -> {
            primaryKeyColumns.append(String.format("__%s:row.%s,",
                    column.columnName,
                    column.columnName
            ));
        });
        primaryKeyColumns.setLength(primaryKeyColumns.length() - 1); // delete trailing comma

        return String.format(
                "CALL apoc.periodic.iterate(\n" +
                        "'%s',\n" +
                        "\"CREATE (n:%s{%s, %s})\",\n" +
                        "{batchSize:10000, parallel:true}) YIELD batches RETURN batches\n",
                loadJdbcCall,
                nodeMapping.getNodeLabel(),
                mappedColumns,
                primaryKeyColumns
        );
    }

    @Override
    protected String createEdgeQuery(SQLSchemaMapping schemaMapping, SQLEdgeMapping edgeMapping) {
        if (edgeMapping instanceof ForeignKeyMapping) {
            return createEdgeFromForeignKeyMapping((ForeignKeyMapping) edgeMapping);
        } else {
            return createEdgeFromJoinTableMapping((JoinTableMapping) edgeMapping);
        }
    }

    private String createEdgeFromForeignKeyMapping(ForeignKeyMapping edgeMapping) {
        String foreignKeyTable = edgeMapping.getForeignKeyTable();
        String fromTable = edgeMapping.getFromTable();
        String toTable = edgeMapping.getToTable();

        List<ForeignKeyInfo> foreignKeys = schemaMetaData.getForeignKeyInfoForTable(
                foreignKeyTable,
                foreignKeyTable.equals(toTable) ? fromTable : toTable
        );

        String loadJdbcCall = String.format("CALL apoc.load.jdbc(\"jdbc:postgresql://%s/%s?user=%s&password=%s\",\"%s\") YIELD row",
                schemaMetaData.getPostgresHost(),
                schemaMetaData.getPostgresDB(),
                schemaMetaData.getPostgresUser(),
                schemaMetaData.getPostgresPassword(),
                foreignKeyTable);

        StringBuilder foreignKeyClause = new StringBuilder();
        for (ForeignKeyInfo foreignKey : foreignKeys) {
            foreignKeyClause.append(String.format(" %s.__%s = row.%s AND",
                    foreignKey.tableName().equals(fromTable) ? "b" : "a",
                    foreignKey.referencedColumnName(),
                    foreignKey.columnName()
            ));
        }
        foreignKeyClause.setLength(foreignKeyClause.length() - 3); // delete trailing AND

        StringBuilder primaryKeyClause = new StringBuilder();
        List<ColumnInfo> primaryKeyColumns = schemaMetaData.getPrimaryKeyColumns(foreignKeyTable);
        for (ColumnInfo primaryKeyColumn : primaryKeyColumns) {
            primaryKeyClause.append(String.format(" %s.__%s = row.%s AND",
                    foreignKeyTable.equals(fromTable) ? "a" : "b",
                    primaryKeyColumn.columnName,
                    primaryKeyColumn.columnName
            ));
        }
        primaryKeyClause.setLength(primaryKeyClause.length() - 3); // delete trailing AND

        String tableNameClause = String.format(" a.__table_name = '%s' AND b.__table_name = '%s'",
                fromTable,
                toTable
        );

        return String.format(
                "CALL apoc.periodic.iterate(\n" +
                        "'%s',\n" +
                        "\"MATCH\n" +
                        "    (a:%s),\n" +
                        "    (b:%s)\n" +
                        "WHERE %s AND %s AND %s\n" +
                        "CREATE (a)-[r:%s]->(b)\",\n" +
                        "{batchSize:10000, parallel:true}) YIELD batches RETURN batches",
                loadJdbcCall,
                edgeMapping.getFromNode(),
                edgeMapping.getToNode(),
                foreignKeyClause,
                primaryKeyClause,
                tableNameClause,
                edgeMapping.getEdgeLabel()
        );
    }

    private String createEdgeFromJoinTableMapping(JoinTableMapping edgeMapping) {
        String loadJdbcCall = String.format("CALL apoc.load.jdbc(\"jdbc:postgresql://%s/%s?user=%s&password=%s\",\"%s\") YIELD row",
                schemaMetaData.getPostgresHost(),
                schemaMetaData.getPostgresDB(),
                schemaMetaData.getPostgresUser(),
                schemaMetaData.getPostgresPassword(),
                edgeMapping.getJoinTable());

        List<ForeignKeyInfo> fromTableForeignKeys = schemaMetaData.getForeignKeyInfoForTable(
                edgeMapping.getJoinTable(),
                edgeMapping.getFromTable()
        );

        List<ForeignKeyInfo> toTableForeignKeys = schemaMetaData.getForeignKeyInfoForTable(
                edgeMapping.getJoinTable(),
                edgeMapping.getToTable()
        );

        StringBuilder fromTableForeignKeyClause = new StringBuilder();
        fromTableForeignKeys.forEach(foreignKey -> {
            fromTableForeignKeyClause.append(String.format(" a.__%s = row.%s AND",
                    foreignKey.referencedColumnName(),
                    foreignKey.columnName()
            ));
        });
        fromTableForeignKeyClause.setLength(fromTableForeignKeyClause.length() - 3);

        StringBuilder toTableForeignKeyClause = new StringBuilder();
        toTableForeignKeys.forEach(foreignKey -> {
            toTableForeignKeyClause.append(String.format(" b.__%s = row.%s AND",
                    foreignKey.referencedColumnName(),
                    foreignKey.columnName()
            ));
        });
        toTableForeignKeyClause.setLength(toTableForeignKeyClause.length() - 3);

        String tableNameClause = String.format(" a.__table_name = '%s' AND b.__table_name = '%s'",
                edgeMapping.getFromTable(),
                edgeMapping.getToTable()
        );

        StringBuilder mappedColumns = new StringBuilder();
        edgeMapping.getMappedColumns().forEach((column, attribute) -> {
            mappedColumns.append(String.format("%s:coalesce(row.%s, 'NULL'),", attribute, column)); // null values
            // represented as 'NULL'
        });
        if (edgeMapping.getMappedColumns().size() != 0)
            mappedColumns.setLength(mappedColumns.length() - 1); // delete trailing comma

        return String.format(
                "CALL apoc.periodic.iterate(" +
                        "'%s'," +
                        "\"MATCH" +
                        "    (a:%s)," +
                        "    (b:%s)" +
                        "WHERE %s AND %s AND %s" +
                        "CREATE (a)-[r:%s{%s}]->(b)\"," +
                        "{batchSize:10000, parallel:true, concurrency:100}) YIELD batches RETURN batches",
                loadJdbcCall,
                edgeMapping.getFromNode(),
                edgeMapping.getToNode(),
                fromTableForeignKeyClause,
                toTableForeignKeyClause,
                tableNameClause,
                edgeMapping.getEdgeLabel(),
                mappedColumns
        );
    }
}
