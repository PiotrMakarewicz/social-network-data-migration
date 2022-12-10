package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.CSVEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.CSVNodeMapping;

import java.util.ArrayList;

public class CsvAddingStrategy extends CsvStrategy {
    public CsvAddingStrategy(String dataPath, String fieldTerminator, boolean withHeaders) {
        super(dataPath, fieldTerminator, withHeaders);
    }

    @Override
    protected String createIndexQuery(CSVNodeMapping nodeMapping) {
        ArrayList<String> properties = new ArrayList<>();

        for (var property : nodeMapping.getMappedColumns().values()) {
            properties.add("p." + property);
        }

        String propertiesStr = String.join(", ", properties);

        return String.format(
                "CREATE INDEX IF NOT EXISTS\n" +
                        "FOR (p:%s)\n" +
                        "ON (%s)\n",
                nodeMapping.getNodeLabel(), propertiesStr);
    }

    @Override
    protected String createEdgeQuery(CSVEdgeMapping edgeMapping, CSVNodeMapping fromNode, CSVNodeMapping toNode) {
        String fromMappedColumns = mappedColumnsToStr(fromNode.getMappedColumns());
        String toMappedColumns = mappedColumnsToStr(toNode.getMappedColumns());
        String edgeMappedColumns = mappedColumnsToStr(edgeMapping.getMappedColumns());

        return String.format(
                "CALL apoc.load.csv('%s', {sep: '%s', header: %s}) YIELD list \n" +
                        "CALL {\n" +
                        "  WITH list\n" +
                        "  MERGE (p1:%s {%s})\n" +
                        "  MERGE (p2:%s {%s})\n" +
                        "  MERGE (p1)-[e:%s {%s}]->(p2)\n" +
                        "} IN TRANSACTIONS OF 10000 ROWS",
                dataPath,
                fieldTerminator,
                withHeaders,
                fromNode.getNodeLabel(),
                fromMappedColumns,
                toNode.getNodeLabel(),
                toMappedColumns,
                edgeMapping.getEdgeLabel(),
                edgeMappedColumns);
    }
}
