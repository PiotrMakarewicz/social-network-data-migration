package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.ForeignKeyMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.JoinTableMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.SQLEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.SQLNodeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.utils.SchemaMetaData;
import pl.edu.agh.socialnetworkdatamigration.core.utils.info.ForeignKeyInfo;

import java.util.List;
import java.util.stream.Collectors;

public class PostgresMergingStrategy extends PostgresStrategy {
    private final String FROM_TABLE_PREFIX = "f__";
    private final String TO_TABLE_PREFIX = "t__";
    private final String JOIN_TABLE_PREFIX = "j__";
    private final String FROM_NODE_VAR = "from_node";
    private final String TO_NODE_VAR = "to_node";

    public PostgresMergingStrategy(SchemaMetaData schemaMetaData) {
        super(schemaMetaData);
    }

    @Override
    protected String createIndexQuery(SQLNodeMapping nodeMapping) {
        String nodeLabel = nodeMapping.getNodeLabel();

        String identifyingFields = nodeMapping.getIdentifyingFields()
                .stream()
                .map(field -> String.format("n.%s", field))
                .collect(Collectors.joining(","));

        return String.format("CREATE INDEX %s IF NOT EXISTS FOR (n:%s) ON (%s)",
                nodeLabel + "_id_fields",
                nodeLabel,
                identifyingFields);
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

        String mappedIdentifyingFields = nodeMapping.getIdentifyingFields()
                .stream()
                .map(field -> String.format("%s:row.%s", field, nodeMapping.getColumnForField(field)))
                .collect(Collectors.joining(", "));

        String mappedNonIdentifyingFields = nodeMapping.getMappedColumns()
                .entrySet()
                .stream()
                .filter(entry -> !nodeMapping.getIdentifyingFields().contains(entry.getValue()))
                .map(entry -> String.format("n.%s=coalesce(row.%s, 'NULL')", entry.getValue(), entry.getKey()))
                .collect(Collectors.joining(", "));

        String onCreateSet;
        if (mappedNonIdentifyingFields.isEmpty()) {
            onCreateSet = "";
        } else {
            onCreateSet = String.format("ON CREATE SET %s ON MATCH SET %s",
                    mappedNonIdentifyingFields,
                    mappedNonIdentifyingFields);
        }

        String mergeNode = String.format("MERGE (n:%s{%s}) %s",
                nodeMapping.getNodeLabel(),
                mappedIdentifyingFields,
                onCreateSet);

        return String.format(
                "CALL apoc.periodic.iterate(\n" +
                        "'%s',\n" +
                        "\"%s\",\n" +
                        "{batchSize:10000, parallel:true, retries:100}) YIELD batches RETURN batches\n\n",
                loadJdbcCall,
                mergeNode
        );
    }

    @Override
    protected String createEdgeQuery(SQLSchemaMapping schemaMapping, SQLEdgeMapping edgeMapping) {
        SQLNodeMapping fromNode = schemaMapping.getNodeForTable(edgeMapping.getFromTable())
                .orElseThrow(() -> new RuntimeException("No node mapping for table " + edgeMapping.getFromTable()));

        SQLNodeMapping toNode = schemaMapping.getNodeForTable(edgeMapping.getToTable())
                .orElseThrow(() -> new RuntimeException("No node mapping for table " + edgeMapping.getToTable()));

        String loadJdbcCall = loadJdbcCall(fromNode, toNode, edgeMapping);

        String fromNodeQuery = nodeMatchClause(fromNode, FROM_NODE_VAR, FROM_TABLE_PREFIX);
        String toNodeQuery = nodeMatchClause(toNode, TO_NODE_VAR, TO_TABLE_PREFIX);
        String mergeEdgeQuery = mergeEdgeQuery(edgeMapping);

        return String.format(
                "CALL apoc.periodic.iterate(\n" +
                        "'%s',\n" +
                        "\"%s\n" +
                        "  %s\n" +
                        "%s\",\n" +
                        "{batchSize:10000, parallel:true, retries:100}) YIELD batches RETURN batches\n\n",
                loadJdbcCall,
                fromNodeQuery,
                toNodeQuery,
                mergeEdgeQuery
        );
    }

    private String nodeMatchClause(SQLNodeMapping node, String nodeVariable, String columnPrefix) {
        String toNodeMappedIdentifyingFields = node.getIdentifyingFields()
                .stream()
                .map(field -> String.format("%s:row.%s%s", field, columnPrefix, node.getColumnForField(field)))
                .collect(Collectors.joining(","));

        return String.format("MATCH (%s:%s{%s})", nodeVariable, node.getNodeLabel(), toNodeMappedIdentifyingFields);
    }

    private String loadJdbcCall(SQLNodeMapping fromNode, SQLNodeMapping toNode, SQLEdgeMapping edgeMapping) {
        String joinSqlQuery;
        if (edgeMapping instanceof ForeignKeyMapping) {
            joinSqlQuery = joinTableSqlQuery(fromNode, toNode, (ForeignKeyMapping) edgeMapping);
        } else {
            joinSqlQuery = joinTableSqlQuery(fromNode, toNode, (JoinTableMapping) edgeMapping);
        }

        return String.format("CALL apoc.load.jdbc(\"jdbc:postgresql://%s/%s?user=%s&password=%s\",\"%s\") YIELD row",
                schemaMetaData.getPostgresHost(),
                schemaMetaData.getPostgresDB(),
                schemaMetaData.getPostgresUser(),
                schemaMetaData.getPostgresPassword(),
                joinSqlQuery);
    }

    private String mergeEdgeQuery(SQLEdgeMapping edgeMapping) {
        if (edgeMapping instanceof ForeignKeyMapping) {
            return mergeEdgeQuery((ForeignKeyMapping) edgeMapping);
        } else {
            return mergeEdgeQuery((JoinTableMapping) edgeMapping);
        }
    }

    private String mergeEdgeQuery(JoinTableMapping edgeMapping) {
        String mappedFields = edgeMapping.getMappedColumns()
                .entrySet()
                .stream()
                .map(entry -> String.format("r.%s=coalesce(row.%s%s, 'NULL')", entry.getValue(), JOIN_TABLE_PREFIX, entry.getKey()))
                .collect(Collectors.joining(","));

        return String.format("MERGE (%s)-[r:%s{%s}]->(%s)", FROM_NODE_VAR, edgeMapping.getEdgeLabel(), mappedFields, TO_NODE_VAR);
    }

    private String mergeEdgeQuery(ForeignKeyMapping edgeMapping) {
        return String.format("MERGE (%s)-[r:%s]->(%s)", FROM_NODE_VAR, edgeMapping.getEdgeLabel(), TO_NODE_VAR);
    }

    private String joinTableSqlQuery(SQLNodeMapping fromNode, SQLNodeMapping toNode, JoinTableMapping edgeMapping) {
        List<ForeignKeyInfo> fromTableForeignKeys = schemaMetaData.getForeignKeyInfoForTable(
                edgeMapping.getJoinTable(),
                edgeMapping.getFromTable()
        );

        List<ForeignKeyInfo> toTableForeignKeys = schemaMetaData.getForeignKeyInfoForTable(
                edgeMapping.getJoinTable(),
                edgeMapping.getToTable()
        );

        String fromTableJoinTableEqualityClause = columnsEqualityClause(fromTableForeignKeys);
        String toTableJoinTableEqualityClause = columnsEqualityClause(toTableForeignKeys);

        String fromTableColumns = identifyingColumnsForNode(fromNode,
                fromNode.getSqlTableName().equals(edgeMapping.getFromTable()) ? FROM_TABLE_PREFIX : TO_TABLE_PREFIX);

        String toTableColumns = identifyingColumnsForNode(toNode,
                toNode.getSqlTableName().equals(edgeMapping.getFromTable()) ? FROM_TABLE_PREFIX : TO_TABLE_PREFIX);

        String joinTableColumns = edgeMapping.getMappedColumns()
                .keySet()
                .stream()
                .map(column -> String.format("%s.%s AS %s%s",
                        edgeMapping.getJoinTable(),
                        column,
                        JOIN_TABLE_PREFIX,
                        column))
                .collect(Collectors.joining(", "));

        return String.format("SELECT %s FROM %s INNER JOIN %s ON %s INNER JOIN %s ON %s",
                joinNonEmpty(", ", List.of(fromTableColumns, toTableColumns, joinTableColumns)),
                edgeMapping.getFromTable(),
                edgeMapping.getJoinTable(),
                fromTableJoinTableEqualityClause,
                edgeMapping.getToTable(),
                toTableJoinTableEqualityClause
        );

    }

    private String joinTableSqlQuery(SQLNodeMapping fromNode, SQLNodeMapping toNode, ForeignKeyMapping edgeMapping) {
        List<ForeignKeyInfo> foreignKeys = schemaMetaData.getForeignKeyInfoForTable(
                edgeMapping.getForeignKeyTable(),
                edgeMapping.getReferencedTable()
        );

        String columnsEqualityClause = columnsEqualityClause(foreignKeys);

        String fromTableColumns = identifyingColumnsForNode(fromNode,
                fromNode.getSqlTableName().equals(edgeMapping.getFromTable()) ? FROM_TABLE_PREFIX : TO_TABLE_PREFIX);

        String toTableColumns = identifyingColumnsForNode(toNode,
                toNode.getSqlTableName().equals(edgeMapping.getFromTable()) ? FROM_TABLE_PREFIX : TO_TABLE_PREFIX);

        return String.format("SELECT %s FROM %s INNER JOIN %s ON %s",
                joinNonEmpty(", ", List.of(fromTableColumns, toTableColumns)),
                edgeMapping.getFromTable(),
                edgeMapping.getToTable(),
                columnsEqualityClause);
    }

    private String columnsEqualityClause(List<ForeignKeyInfo> foreignKeys) {
        return foreignKeys
                .stream()
                .map(fk -> String.format("%s.%s = %s.%s",
                        fk.foreignKeyColumn.tableName,
                        fk.foreignKeyColumn.columnName,
                        fk.referencedColumn.tableName,
                        fk.referencedColumn.columnName))
                .collect(Collectors.joining(" AND "));
    }

    private String identifyingColumnsForNode(SQLNodeMapping node, String tablePrefix) {
        return node.getIdentifyingFields()
                .stream()
                .map(field -> String.format("%s.%s AS %s%s",
                        node.getSqlTableName(),
                        node.getColumnForField(field),
                        tablePrefix,
                        node.getColumnForField(field)))
                .collect(Collectors.joining(", "));
    }

    private String joinNonEmpty(String delimiter, List<String> strings) {
        return strings.stream()
                .filter(s -> !s.isEmpty())
                .collect(Collectors.joining(delimiter));
    }
}
