package migrator;

import mapping.CSVSchemaMapping;
import mapping.SchemaMapping;
import mapping.edge.CSVEdgeMapping;
import mapping.edge.EdgeMapping;
import mapping.edge.NoHeadersCSVEdgeMapping;
import mapping.node.CSVNodeMapping;
import mapping.node.NoHeadersCSVNodeMapping;
import mapping.node.NodeMapping;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

public class CSVMigrator implements Migrator, AutoCloseable {
    private final String configPath;
    private final String dataPath;
    private final boolean withHeaders;
    private Driver neo4jDriver;

    private final String fieldTerminator = "\\t";

    public CSVMigrator(String configPath, String dataPath, boolean withHeaders) throws IOException {
        this.configPath = configPath;
        this.dataPath = dataPath;
        this.withHeaders = withHeaders;
        if (configPath != null) {
            Properties properties = new Properties();
            properties.load(new FileInputStream(configPath));
            String neo4jHost = properties.getProperty("neo4jHost");
            String neo4jUser = properties.getProperty("neo4jUser");
            String neo4jPassword = properties.getProperty("neo4jPassword");
            this.neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword));
        }
    }

    @Override
    public void migrateData(SchemaMapping schemaMapping)  {
        var csvSchemaMapping = (CSVSchemaMapping) schemaMapping;
        var csvEdgeMapping = (CSVEdgeMapping) csvSchemaMapping.getEdgeMappings().stream().findFirst().get();
        var fromNodeMapping = (CSVNodeMapping) csvEdgeMapping.getFromNodeMapping();
        var toNodeMapping = (CSVNodeMapping) csvEdgeMapping.getToNodeMapping();

        createIndex(fromNodeMapping);
        createIndex(toNodeMapping);

        loadCSV(csvEdgeMapping, fromNodeMapping, toNodeMapping);

    }

    private void createIndex(CSVNodeMapping nodeMapping) {
        ArrayList<String> properties = new ArrayList<>();

        for (var property: nodeMapping.getMappedColumns().values()){
            properties.add("p." + property);
        }

        String propertiesStr = String.join(", ", properties);

        String query = """
                CREATE INDEX IF NOT EXISTS
                FOR (p:%s)
                ON (%s)
                """
                .formatted(nodeMapping.getNodeLabel(), propertiesStr);

        this.execute(query);
    }

    private void loadCSV(EdgeMapping csvEdgeMapping, NodeMapping fromNodeMapping, NodeMapping toNodeMapping) {
        this.execute(buildLoadCsvQuery(csvEdgeMapping, fromNodeMapping, toNodeMapping));
    }

    public String buildLoadCsvQuery(EdgeMapping csvEdgeMapping, NodeMapping fromNodeMapping, NodeMapping toNodeMapping) {
        String fromMappedColumns = mappedColumnsToStr(fromNodeMapping);
        String toMappedColumns = mappedColumnsToStr(toNodeMapping);
        String edgeMappedColumns = mappedColumnsToStr(csvEdgeMapping);

        return """
                LOAD CSV%s
                    FROM '%s'
                    AS line
                    FIELDTERMINATOR '%s'
                MERGE (p1:%s {%s})
                MERGE (p2:%s {%s})
                CREATE (p1)-[e:%s {%s}]->(p2)
                """
                .formatted(
                        withHeaders ? " WITH HEADERS" : "",
                        this.dataPath,
                        this.fieldTerminator,
                        fromNodeMapping.getNodeLabel(),
                        fromMappedColumns,
                        toNodeMapping.getNodeLabel(),
                        toMappedColumns,
                        csvEdgeMapping.getEdgeLabel(),
                        edgeMappedColumns);
    }

    private String mappedColumnsToStr(NodeMapping nodeMapping) {
        if (withHeaders)
            return mappedColumnsToStr(((CSVNodeMapping) nodeMapping).getMappedColumns());
        else
            return mappedIndexedColumnsToStr(((NoHeadersCSVNodeMapping) nodeMapping).getMappedColumns());
    }

    private String mappedColumnsToStr(EdgeMapping edgeMapping) {
        if (withHeaders)
            return mappedColumnsToStr(((CSVEdgeMapping) edgeMapping).getMappedColumns());
        else
            return mappedIndexedColumnsToStr(((NoHeadersCSVEdgeMapping) edgeMapping).getMappedColumns());
    }

    private String mappedIndexedColumnsToStr(Map<Integer, String> mappedColumns){
        ArrayList<String> chunks = new ArrayList<>();
        for (var column: mappedColumns.keySet()){
            var attribute = mappedColumns.get(column);
            var chunk = "%s: line[%d]".formatted(attribute, column);
            chunks.add(chunk);
        }
        return String.join(", ", chunks);
    }

    private String mappedColumnsToStr(Map<String, String> mappedColumns){
        ArrayList<String> chunks = new ArrayList<>();
        for (var column: mappedColumns.keySet()){
            var attribute = mappedColumns.get(column);
            var chunk = "%s: line.%s".formatted(attribute, column);
            chunks.add(chunk);
        }
        return String.join(", ", chunks);
    }

    @Override
    public void close() {
        this.neo4jDriver.close();
    }

    private void execute(String query) {
        System.out.println(query);

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
}
