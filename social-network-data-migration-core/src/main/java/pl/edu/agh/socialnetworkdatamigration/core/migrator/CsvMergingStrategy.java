package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.CSVEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.CSVNodeMapping;

import java.util.stream.Collectors;

public class CsvMergingStrategy extends CsvStrategy {
    public CsvMergingStrategy(String dataPath, String fieldTerminator, boolean withHeaders) {
        super(dataPath, fieldTerminator, withHeaders);
    }

    @Override
    protected String createIndexQuery(CSVNodeMapping nodeMapping) {
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
    protected String createEdgeQuery(CSVEdgeMapping edgeMapping, CSVNodeMapping fromNode, CSVNodeMapping toNode) {
        String fromMappedIdentifyingFields = mappedIdentifyingFields(fromNode);
        String fromMappedNonIdentifyingFields = mappedNonIdentifyingFields(fromNode);
        String toMappedIdentifyingFields = mappedIdentifyingFields(toNode);
        String toMappedNonIdentifyingFields = mappedNonIdentifyingFields(toNode);
        String edgeMappedColumns = mappedColumnsToStr(edgeMapping.getMappedColumns());

        String fromNodeOnCreate = onCreateClause(fromMappedNonIdentifyingFields);
        String toNodeOnCreate = onCreateClause(toMappedNonIdentifyingFields);

        return String.format(
                "CALL apoc.load.csv('%s', {sep: '%s', header: %s}) YIELD list \n" +
                        "CALL {\n" +
                        "  WITH list\n" +
                        "  MERGE (p1:%s {%s}) %s\n" +
                        "  MERGE (p2:%s {%s}) %s\n" +
                        "  MERGE (p1)-[e:%s {%s}]->(p2)\n" +
                        "} IN TRANSACTIONS OF 10000 ROWS",
                dataPath,
                fieldTerminator,
                withHeaders,
                fromNode.getNodeLabel(),
                fromMappedIdentifyingFields,
                fromNodeOnCreate,
                toNode.getNodeLabel(),
                toMappedIdentifyingFields,
                toNodeOnCreate,
                edgeMapping.getEdgeLabel(),
                edgeMappedColumns
        );
    }

    private String onCreateClause(String mappedNonIdentifyingFields) {
        String onCreateSet;
        if (mappedNonIdentifyingFields.isEmpty()) {
            onCreateSet = "";
        } else {
            onCreateSet = String.format("ON CREATE SET %s ON MATCH SET %s",
                    mappedNonIdentifyingFields,
                    mappedNonIdentifyingFields);
        }
        return onCreateSet;
    }

    private String mappedIdentifyingFields(CSVNodeMapping nodeMapping) {
        return nodeMapping.getMappedColumns()
                .entrySet()
                .stream()
                .filter(e -> nodeMapping.getIdentifyingFields().contains(e.getValue()))
                .map(e -> String.format("%s: coalesce(list[%d], 'NULL')", e.getValue(), e.getKey()))
                .collect(Collectors.joining(", "));
    }

    private String mappedNonIdentifyingFields(CSVNodeMapping nodeMapping) {
        return nodeMapping.getMappedColumns()
                .entrySet()
                .stream()
                .filter(e -> !nodeMapping.getIdentifyingFields().contains(e.getValue()))
                .map(e -> String.format("%s = coalesce(list[%d], 'NULL')", e.getValue(), e.getKey()))
                .collect(Collectors.joining(", "));
    }

}
