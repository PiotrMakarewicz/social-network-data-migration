package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.CSVEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.CSVNodeMapping;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// Użycie CSVMigratora wymaga wykomentowania linijki dbms.directories.import w neo4j.conf
public class CSVMigrator extends Migrator<CSVSchemaMapping> {
    private final String dataPath;
    private final String fieldTerminator = "\\t";

    public CSVMigrator(String dataPath, String neo4jHost, String neo4jUser, String neo4jPassword) throws IOException {
        super(new Neo4jQueryExecutor(GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword))));
        this.dataPath = constructNeo4jDataPath(dataPath);
    }

    public static CSVMigrator createFrom(String configPath, String dataPath) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));
        return new CSVMigrator(
                dataPath,
                properties.getProperty("neo4jHost"),
                properties.getProperty("neo4jUser"),
                properties.getProperty("neo4jPassword")
        );
    }

    public void migrateData(CSVSchemaMapping csvSchemaMapping)  {
        var csvEdgeMapping = csvSchemaMapping.getEdgeMapping();
        var fromNodeMapping = csvSchemaMapping.getFromNodeMapping();
        var toNodeMapping = csvSchemaMapping.getToNodeMapping();

        var createIndexQueries = List.of(
                buildCreateIndexQuery(fromNodeMapping),
                buildCreateIndexQuery(toNodeMapping)
        );
        this.executor.executeInOneTransaction(createIndexQueries);

        String loadCsvQuery = buildLoadCsvQuery(csvEdgeMapping, fromNodeMapping, toNodeMapping);
        this.executor.execute(loadCsvQuery);
    }

    private String buildCreateIndexQuery(CSVNodeMapping nodeMapping) {
        ArrayList<String> properties = new ArrayList<>();

        for (var property: nodeMapping.getMappedColumns().values()){
            properties.add("p." + property);
        }

        String propertiesStr = String.join(", ", properties);

        return String.format(
                "CREATE INDEX IF NOT EXISTS\n" +
                "FOR (p:%s)\n" +
                "ON (%s)\n",
                nodeMapping.getNodeLabel(), propertiesStr);
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

    public String constructNeo4jDataPath(String dataPath){
        if (dataPath.charAt(0) == '/')
            return "file://" + dataPath;
        else
            return "file://" + System.getProperty("user.dir") + "/" + dataPath;
    }
}
