package migrator;

import mapping.SQLSchemaMapping;
import mapping.edge.EdgeMapping;
import mapping.edge.ForeignKeyMapping;
import mapping.edge.JoinTableMapping;
import mapping.node.NodeMapping;
import mapping.node.SQLNodeMapping;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import utils.SchemaMetaData;
import utils.info.ColumnInfo;
import utils.info.ForeignKeyInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public class PostgresMigrator implements Migrator<SQLSchemaMapping> {
    private final Connection connection;
    private final Driver neo4jDriver;
    private final String postgresHost;
    private final String postgresDB;
    private final String postgresUser;
    private final String postgresPassword;
    private final SchemaMetaData schemaMetaData;
    private boolean dryRun;

    public PostgresMigrator(String configPath) throws IOException, SQLException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));
        this.postgresHost = properties.getProperty("postgresHost");
        this.postgresDB = properties.getProperty("postgresDB");
        this.postgresUser = properties.getProperty("postgresUser");
        this.postgresPassword = properties.getProperty("postgresPassword");
        String neo4jHost = properties.getProperty("neo4jHost");
        String neo4jUser = properties.getProperty("neo4jUser");
        String neo4jPassword = properties.getProperty("neo4jPassword");

        this.connection = DriverManager.getConnection("jdbc:postgresql://" + postgresHost + "/" +
                postgresDB, postgresUser, postgresPassword);
        this.neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword));
        this.schemaMetaData = new SchemaMetaData(postgresHost, postgresDB, postgresUser, postgresPassword);
    }

    public PostgresMigrator(String configPath, boolean dryRun) throws SQLException, IOException {
        this(configPath);
        this.dryRun = dryRun;
    }

    public void migrateData(SQLSchemaMapping schemaMapping) {
        schemaMapping.getNodeMappings().forEach(this::createNode);
        schemaMapping.getEdgeMappings().forEach(this::createEdge);
    }

    private void createNode(NodeMapping nodeMapping) {
        SQLNodeMapping sqlNodeMapping = (SQLNodeMapping) nodeMapping;
        this.createIndex(sqlNodeMapping);

        String tableName = sqlNodeMapping.getSqlTableName();

        String loadJdbcCall = String.format("CALL apoc.load.jdbc(\"jdbc:postgresql://%s/%s?user=%s&password=%s\",\"%s\") YIELD row",
                        postgresHost,
                        postgresDB,
                        postgresUser,
                        postgresPassword,
                        tableName);

        StringBuilder mappedColumns = new StringBuilder();
        mappedColumns.append(String.format("__table_name:'%s',", tableName));
        sqlNodeMapping.getMappedColumns().forEach((column, attribute) -> {
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

        String call = String.format(
                "CALL apoc.periodic.iterate(\n" +
                "'%s',\n" +
                "\"CREATE (n:%s{%s, %s})\",\n" +
                "{batchSize:10000, parallel:true, concurrency:100}) YIELD batches RETURN batches\n",
                    loadJdbcCall,
                    sqlNodeMapping.getNodeLabel(),
                    mappedColumns,
                    primaryKeyColumns
                );

        this.execute(call);
    }

    private void createEdge(EdgeMapping edgeMapping) {
        if (edgeMapping instanceof ForeignKeyMapping) {
            createEdgeFromForeignKeyMapping((ForeignKeyMapping) edgeMapping);
        } else if (edgeMapping instanceof JoinTableMapping) {
            createEdgeFromJoinTableMapping((JoinTableMapping) edgeMapping);
        }
    }

    private void createEdgeFromForeignKeyMapping(ForeignKeyMapping edgeMapping) {
        String foreignKeyTable = edgeMapping.getForeignKeyTable();
        String fromTable = edgeMapping.getFromTable();
        String toTable = edgeMapping.getToTable();

        List<ForeignKeyInfo> foreignKeys = schemaMetaData.getForeignKeyInfoForTable(
                foreignKeyTable,
                foreignKeyTable.equals(toTable) ? fromTable : toTable
        );

        String loadJdbcCall = String.format("CALL apoc.load.jdbc(\"jdbc:postgresql://%s/%s?user=%s&password=%s\",\"%s\") YIELD row",
                        postgresHost,
                        postgresDB,
                        postgresUser,
                        postgresPassword,
                        foreignKeyTable);

        StringBuilder foreignKeyClause = new StringBuilder();
        for (ForeignKeyInfo foreignKey : foreignKeys){
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

        String call = String.format(
                "CALL apoc.periodic.iterate(\n" +
                "'%s',\n" +
                "\"MATCH\n" +
                "    (a:%s),\n" +
                "    (b:%s)\n" +
                "WHERE %s AND %s AND %s\n" +
                "CREATE (a)-[r:%s]->(b)\",\n" +
                "{batchSize:10000, parallel:true, concurrency:100}) YIELD batches RETURN batches",
                    loadJdbcCall,
                    edgeMapping.getFromNode(),
                    edgeMapping.getToNode(),
                    foreignKeyClause,
                    primaryKeyClause,
                    tableNameClause,
                    edgeMapping.getEdgeLabel()
                );

        this.execute(call);
    }

    private void createEdgeFromJoinTableMapping(JoinTableMapping edgeMapping) {
        String loadJdbcCall = String.format("CALL apoc.load.jdbc(\"jdbc:postgresql://%s/%s?user=%s&password=%s\",\"%s\") YIELD row",
                        postgresHost,
                        postgresDB,
                        postgresUser,
                        postgresPassword,
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

        String call = String.format(
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

        this.execute(call);
    }

    private void execute(String query) {
        System.out.println(query);

        if (this.dryRun)
            return;

        long start = System.currentTimeMillis();
        try (Session session = neo4jDriver.session()){
            session.writeTransaction(tx -> {
                tx.run(query);
                return null;
            });
        }
        long end = System.currentTimeMillis();
        System.out.printf("Time taken: %s s%n", (end - start) / 1000);
    }

    private void createIndex(SQLNodeMapping nodeMapping) {
        String nodeLabel = nodeMapping.getNodeLabel();
        String tableName = nodeMapping.getSqlTableName();
        List<String> primaryKeyColumns = schemaMetaData
                .getPrimaryKeyColumns(tableName)
                .stream()
                .map(column -> column.columnName)
                .collect(Collectors.toList());

        String call = String.format("CREATE INDEX %s IF NOT EXISTS FOR (n:%s) ON (",
                (nodeLabel + "_" + tableName),
                nodeLabel);
        StringBuilder callBuilder = new StringBuilder(call);
        primaryKeyColumns.forEach(column -> {
            callBuilder.append(String.format("n.__%s,", column));
        });
        callBuilder.append("n.__table_name)");
        this.execute(callBuilder.toString());
    }

    @Override
    public void close() throws Exception {
        this.connection.close();
        this.neo4jDriver.close();
        this.schemaMetaData.close();
    }
}
