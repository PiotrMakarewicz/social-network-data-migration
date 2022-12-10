package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.CSVEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.CSVNodeMapping;

import java.util.*;

public abstract class CsvStrategy extends MigrationStrategy<CSVSchemaMapping> {
    protected final String dataPath;
    protected final String fieldTerminator;
    protected final boolean withHeaders;

    protected CsvStrategy(String dataPath, String fieldTerminator, boolean withHeaders) {
        this.dataPath = constructNeo4jDataPath(dataPath);
        this.fieldTerminator = fieldTerminator;
        this.withHeaders = withHeaders;
    }

    @Override
    public List<Set<String>> createMigrationQueries(CSVSchemaMapping schemaMapping) {
        List<Set<String>> operations = new ArrayList<>();

        CSVNodeMapping fromNode = schemaMapping.getFromNodeMapping();
        CSVNodeMapping toNode = schemaMapping.getToNodeMapping();
        CSVEdgeMapping edgeMapping = schemaMapping.getEdgeMapping();

        Set<String> indexCreationOps = new HashSet<>();
        indexCreationOps.add(createIndexQuery(fromNode));
        indexCreationOps.add(createIndexQuery(toNode));

        operations.add(indexCreationOps);
        operations.add(Set.of(
                createEdgeQuery(edgeMapping, fromNode, toNode)
        ));

        return operations;
    }

    protected abstract String createIndexQuery(CSVNodeMapping nodeMapping);

    protected abstract String createEdgeQuery(CSVEdgeMapping edgeMapping, CSVNodeMapping fromNode, CSVNodeMapping toNode);

    protected String mappedColumnsToStr(Map<Integer, String> mappedColumns) {
        ArrayList<String> chunks = new ArrayList<>();
        for (var column : mappedColumns.keySet()) {
            var attribute = mappedColumns.get(column);
            var chunk = String.format("%s: list[%d]", attribute, column);
            chunks.add(chunk);
        }
        return String.join(", ", chunks);
    }

    protected String constructNeo4jDataPath(String dataPath) {
        if (dataPath.charAt(0) == '/')
            return "file://" + dataPath;
        else
            return "file://" + System.getProperty("user.dir") + "/" + dataPath;
    }
}
