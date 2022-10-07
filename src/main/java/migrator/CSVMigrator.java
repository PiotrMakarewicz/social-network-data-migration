package migrator;

import mapping.CSVSchemaMapping;
import mapping.edge.CSVEdgeMapping;
import mapping.node.CSVNodeMapping;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Properties;

// Użycie CSVMigratora wymaga wykomentowania linijki dbms.directories.import w neo4j.conf
public class CSVMigrator implements Migrator<CSVSchemaMapping> {
    private final String configPath;
    private final String dataPath;
    private Driver neo4jDriver;

    private final String fieldTerminator = "\\t";

    public CSVMigrator(String configPath, String dataPath, boolean withHeaders) throws IOException {
        this.configPath = configPath;
        this.dataPath = constructNeo4jDataPath(dataPath);
        if (configPath != null) {
            Properties properties = new Properties();
            properties.load(new FileInputStream(configPath));
            String neo4jHost = properties.getProperty("neo4jHost");
            String neo4jUser = properties.getProperty("neo4jUser");
            String neo4jPassword = properties.getProperty("neo4jPassword");
            this.neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword));
        }
    }

    public void migrateData(CSVSchemaMapping csvSchemaMapping)  {
        var csvEdgeMapping = csvSchemaMapping.getEdgeMapping();
        var fromNodeMapping = csvSchemaMapping.getFromNodeMapping();
        var toNodeMapping = csvSchemaMapping.getToNodeMapping();

        createIndex(fromNodeMapping);
        createIndex(toNodeMapping);

        this.execute(buildLoadCsvQuery(csvEdgeMapping, fromNodeMapping, toNodeMapping));
    }

    private void createIndex(CSVNodeMapping nodeMapping) {
        ArrayList<String> properties = new ArrayList<>();

        for (var property: nodeMapping.getMappedColumns().values()){
            properties.add("p." + property);
        }

        String propertiesStr = String.join(", ", properties);

        String query = String.format(
                "CREATE INDEX IF NOT EXISTS\n" +
                "FOR (p:%s)\n" +
                "ON (%s)\n",
                nodeMapping.getNodeLabel(), propertiesStr);

        this.execute(query);
    }

    public String buildLoadCsvQuery(CSVEdgeMapping csvEdgeMapping, CSVNodeMapping fromNodeMapping,
                                    CSVNodeMapping toNodeMapping) {
        String fromMappedColumns = mappedColumnsToStr(fromNodeMapping.getMappedColumns());
        String toMappedColumns = mappedColumnsToStr(toNodeMapping.getMappedColumns());
        String edgeMappedColumns = mappedColumnsToStr(csvEdgeMapping.getMappedColumns());

        return String.format(
                "LOAD CSV\n" +
                "    FROM '%s'\n" +
                "    AS line\n" +
                "    FIELDTERMINATOR '%s'\n" +
                "MERGE (p1:%s {%s})\n" +
                "MERGE (p2:%s {%s})\n" +
                "CREATE (p1)-[e:%s {%s}]->(p2)",
                        this.dataPath,
                        this.fieldTerminator,
                        fromNodeMapping.getNodeLabel(),
                        fromMappedColumns,
                        toNodeMapping.getNodeLabel(),
                        toMappedColumns,
                        csvEdgeMapping.getEdgeLabel(),
                        edgeMappedColumns);
    }

    private String mappedColumnsToStr(Map<Integer, String> mappedColumns){
        ArrayList<String> chunks = new ArrayList<>();
        for (var column: mappedColumns.keySet()){
            var attribute = mappedColumns.get(column);
            var chunk = String.format("%s: line[%d]", attribute, column);
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
        System.out.printf("Query execution time: %s ms%n%n", (end - start));
    }

    public String constructNeo4jDataPath(String dataPath){
        if (dataPath.charAt(0) == '/')
            return "file://" + dataPath;
        else
            return "file://" + System.getProperty("user.dir") + "/" + dataPath;
    }
}
