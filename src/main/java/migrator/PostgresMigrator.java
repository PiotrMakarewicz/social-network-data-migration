package migrator;

import mapping.SchemaMapping;
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
import utils.SchemaMetaData.FKColumnInfo;
import utils.SchemaMetaData.ColumnInfo;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class PostgresMigrator implements Migrator, AutoCloseable {
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

    @Override
    public void migrateData(SchemaMapping schemaMapping) {
        schemaMapping.getNodeMappings().forEach(this::createNode);
        schemaMapping.getEdgeMappings().forEach(this::createEdge);
    }

    private void createNode(NodeMapping nodeMapping) {
        SQLNodeMapping sqlNodeMapping = (SQLNodeMapping) nodeMapping;

        this.createIndex(sqlNodeMapping);

        String call = """
                CALL apoc.periodic.iterate(
                'CALL apoc.load.jdbc("jdbc:postgresql://%s/%s?user=%s&password=%s","%s") YIELD row',
                "CREATE (n:%s{
                """.formatted(
                    postgresHost,
                    postgresDB,
                    postgresUser,
                    postgresPassword,
                    sqlNodeMapping.getSqlTableName(),
                    sqlNodeMapping.getNodeLabel()
                );
        StringBuilder callBuilder = new StringBuilder(call);
        callBuilder.append("__table_name:'%s',".formatted(sqlNodeMapping.getSqlTableName()));
        sqlNodeMapping.getMappedColumns().forEach((column, attribute) -> {
            callBuilder.append("%s:coalesce(row.%s, 'NULL'),".formatted(attribute, column)); // null values represented as 'NULL'
        });

        List<ColumnInfo> primaryKeyColumns = schemaMetaData.getPrimaryKeyColumns(sqlNodeMapping.getSqlTableName());
        primaryKeyColumns.forEach(column -> {
            callBuilder.append("__%s:row.%s,".formatted(
                    column.columnName,
                    column.columnName
            ));
        });

        callBuilder.setLength(callBuilder.length() - 1); // delete trailing comma
        callBuilder.append("""
                })",
                {batchSize:10000, parallel:true, concurrency:100}) YIELD batches RETURN batches
                """);

        this.execute(callBuilder.toString());
    }

    private void createEdge(EdgeMapping edgeMapping) {
        if (edgeMapping instanceof ForeignKeyMapping) {
            createEdgeFromForeignKeyMapping((ForeignKeyMapping) edgeMapping);
        } else if (edgeMapping instanceof JoinTableMapping) {
            createEdgeFromJoinTableMapping((JoinTableMapping) edgeMapping);
        }
    }

    private void createEdgeFromForeignKeyMapping(ForeignKeyMapping edgeMapping) {
        String call = """
                CALL apoc.periodic.iterate(
                'CALL apoc.load.jdbc("jdbc:postgresql://%s/%s?user=%s&password=%s","%s") YIELD row',
                "MATCH
                    (a:%s),
                    (b:%s)
                WHERE
                """.formatted(
                    postgresHost,
                    postgresDB,
                    postgresUser,
                    postgresPassword,
                    edgeMapping.getForeignKeyTable(),
                    edgeMapping.getFromNode(),
                    edgeMapping.getToNode()
                );
        StringBuilder callBuilder = new StringBuilder(call);

        String foreignKeyTable = edgeMapping.getForeignKeyTable();
        String fromTable = edgeMapping.getFromTable();
        String toTable = edgeMapping.getToTable();

        List<FKColumnInfo> foreignKeys = schemaMetaData.getForeignKeyColumnsForTable(
                foreignKeyTable,
                foreignKeyTable.equals(toTable) ? fromTable : toTable
        );
        for (FKColumnInfo foreignKey : foreignKeys){
            if (foreignKey.tableName().equals(edgeMapping.getFromTable())) {
                callBuilder.append(" b.__%s = row.%s AND".formatted(
                        foreignKey.referencedColumnName(),
                        foreignKey.columnName()
                ));
            } else {
                callBuilder.append(" a.__%s = row.%s AND".formatted(
                        foreignKey.referencedColumnName(),
                        foreignKey.columnName()
                ));
            }
        }

        List<ColumnInfo> primaryKeyColumns = schemaMetaData.getPrimaryKeyColumns(edgeMapping.getForeignKeyTable());
        for (ColumnInfo primaryKeyColumn : primaryKeyColumns) {
            if (edgeMapping.getForeignKeyTable().equals(edgeMapping.getFromTable())) {
                callBuilder.append(" a.__%s = row.%s AND".formatted(
                        primaryKeyColumn.columnName,
                        primaryKeyColumn.columnName
                ));
            } else {
                callBuilder.append(" b.__%s = row.%s AND".formatted(
                        primaryKeyColumn.columnName,
                        primaryKeyColumn.columnName
                ));
            }
        }


        callBuilder.append(" a.__table_name = '%s' AND b.__table_name = '%s'".formatted(
                edgeMapping.getFromTable(),
                edgeMapping.getToTable()
        ));
        callBuilder.append(" CREATE (a)-[r:%s]->(b)".formatted(edgeMapping.getEdgeLabel()));
        callBuilder.append("""
                ", {batchSize:10000, parallel:true, concurrency:100}) YIELD batches RETURN batches
                """);

        this.execute(callBuilder.toString());
    }

    private void createEdgeFromJoinTableMapping(JoinTableMapping edgeMapping) {
        String call = """
                CALL apoc.periodic.iterate(
                'CALL apoc.load.jdbc("jdbc:postgresql://%s/%s?user=%s&password=%s","%s") YIELD row',
                "MATCH
                    (a:%s),
                    (b:%s)
                WHERE
                """.formatted(
                postgresHost,
                postgresDB,
                postgresUser,
                postgresPassword,
                edgeMapping.getJoinTable(),
                edgeMapping.getFromNode(),
                edgeMapping.getToNode()
        );
        StringBuilder callBuilder = new StringBuilder(call);

        List<FKColumnInfo> fromTableForeignKeys = schemaMetaData.getForeignKeyColumnsForTable(
                edgeMapping.getJoinTable(),
                edgeMapping.getFromTable()
        );
        List<FKColumnInfo> toTableForeignKeys = schemaMetaData.getForeignKeyColumnsForTable(
                edgeMapping.getJoinTable(),
                edgeMapping.getToTable()
        );

        fromTableForeignKeys.forEach(foreignKey -> {
            callBuilder.append(" a.__%s = row.%s AND".formatted(
                    foreignKey.referencedColumnName(),
                    foreignKey.columnName()
            ));
        });

        toTableForeignKeys.forEach(foreignKey -> {
            callBuilder.append(" b.__%s = row.%s AND".formatted(
                    foreignKey.referencedColumnName(),
                    foreignKey.columnName()
            ));
        });

        callBuilder.append(" a.__table_name = '%s' AND b.__table_name = '%s'".formatted(
                edgeMapping.getFromTable(),
                edgeMapping.getToTable()
        ));
        callBuilder.append(" CREATE (a)-[r:%s{".formatted(edgeMapping.getEdgeLabel()));
        edgeMapping.getMappedColumns().forEach((column, attribute) -> {
            callBuilder.append("%s:coalesce(row.%s, 'NULL'),".formatted(attribute, column)); // null values represented as 'NULL'
        });
        if (edgeMapping.getMappedColumns().size() != 0)
            callBuilder.setLength(callBuilder.length() - 1); // delete trailing comma
        callBuilder.append("}]->(b)");
        callBuilder.append("""
                ", {batchSize:10000, parallel:true, concurrency:100}) YIELD batches RETURN batches
                """);

        this.execute(callBuilder.toString());
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
        System.out.println("Time taken: %s s".formatted((end - start) / 1000));
    }

    private void createIndex(SQLNodeMapping nodeMapping) {
        String nodeLabel = nodeMapping.getNodeLabel();
        String tableName = nodeMapping.getSqlTableName();
        List<String> primaryKeyColumns = schemaMetaData
                .getPrimaryKeyColumns(tableName)
                .stream()
                .map(column -> column.columnName)
                .toList();

        String call = "CREATE INDEX %s IF NOT EXISTS FOR (n:%s) ON ("
                .formatted((nodeLabel + "_" + tableName), nodeLabel);
        StringBuilder callBuilder = new StringBuilder(call);
        primaryKeyColumns.forEach(column -> {
            callBuilder.append("n.__%s,".formatted(column));
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
