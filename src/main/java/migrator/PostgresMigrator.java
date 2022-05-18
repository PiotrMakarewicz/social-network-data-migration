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

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class PostgresMigrator implements Migrator, AutoCloseable {
    private final Connection connection;
    private final Driver neo4jDriver;
    private final String postgresHost;
    private final String postgresDB;
    private final String postgresUser;
    private final String postgresPassword;
    private final SchemaMetaData schemaMetaData;

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

    @Override
    public void migrateData(SchemaMapping schemaMapping) {
        schemaMapping.getNodeMappings().forEach(this::createNode);
        schemaMapping.getEdgeMappings().forEach(this::createEdge);
    }

    private void createNode(NodeMapping nodeMapping) {
        SQLNodeMapping sqlNodeMapping = (SQLNodeMapping) nodeMapping;
        String call = """
                CALL apoc.periodic.iterate(
                'CALL apoc.load.jdbc("jdbc:postgresql://%s/%s?user=%s&password=%s","%s") YIELD row',
                "MERGE (n:%s{
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
        callBuilder.setLength(callBuilder.length() - 1); // delete trailing comma
        callBuilder.append("""
                })",
                {batchSize:10000, parallel:true}) YIELD batches RETURN batches
                """);

        try (Session session = neo4jDriver.session()){
            session.writeTransaction(tx -> {
                tx.run(callBuilder.toString());
                return null;
            });
        }
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
                    edgeMapping.getTableFromForeignKey(),
                    edgeMapping.getFromNode(),
                    edgeMapping.getToNode()
                );
        StringBuilder callBuilder = new StringBuilder(call);
        if (edgeMapping.getTableFromForeignKey().equals(edgeMapping.getFromTable())) {
            callBuilder.append(" a.id = row.%s AND b.id = row.%s".formatted(
                    schemaMetaData.getPrimaryKeyColumn(edgeMapping.getFromTable()),
                    edgeMapping.getColumnFromForeignKey()
                    ));
        } else {
            callBuilder.append(" a.id = row.%s AND b.id = row.%s".formatted(
                    edgeMapping.getColumnFromForeignKey(),
                    schemaMetaData.getPrimaryKeyColumn(edgeMapping.getToTable())
                    ));
        }
        callBuilder.append(" AND a.__table_name = '%s' AND b.__table_name = '%s'".formatted(
                edgeMapping.getFromTable(),
                edgeMapping.getToTable()
        ));
        callBuilder.append(" CREATE (a)-[r:%s]->(b)".formatted(edgeMapping.getEdgeLabel()));
        callBuilder.append("""
                ", {batchSize:10000, parallel:true}) YIELD batches RETURN batches
                """);

        try (Session session = neo4jDriver.session()){
            session.writeTransaction(tx -> {
                tx.run(callBuilder.toString());
                return null;
            });
        }
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

        String fromTableForeignKey = schemaMetaData.getForeignKeyColumnName(
                edgeMapping.getJoinTable(),
                edgeMapping.getFromTable()
        );
        String toTableForeignKey = schemaMetaData.getForeignKeyColumnName(
                edgeMapping.getJoinTable(),
                edgeMapping.getToTable()
        );

        callBuilder.append(" a.id = row.%s and b.id = row.%s".formatted(
                fromTableForeignKey,
                toTableForeignKey
        ));
        callBuilder.append(" AND a.__table_name = '%s' AND b.__table_name = '%s'".formatted(
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
                ", {batchSize:10000, parallel:true}) YIELD batches RETURN batches
                """);

        try (Session session = neo4jDriver.session()){
            session.writeTransaction(tx -> {
                tx.run(callBuilder.toString());
                return null;
            });
        }
    }

    @Override
    public void close() throws Exception {
        this.connection.close();
        this.neo4jDriver.close();
        this.schemaMetaData.close();
    }
}
