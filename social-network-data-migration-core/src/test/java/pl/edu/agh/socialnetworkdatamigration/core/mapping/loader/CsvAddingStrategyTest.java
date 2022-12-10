package pl.edu.agh.socialnetworkdatamigration.core.mapping.loader;

import org.junit.jupiter.api.Test;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.CSVEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.CSVNodeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.migrator.CsvAddingStrategy;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CsvAddingStrategyTest {
    @Test
    void buildLoadCsvWithHeadersQuery() throws IOException {
        CsvAddingStrategy migrationStrategy = new CsvAddingStrategy("/data.csv", "\\t", false);

        //when
        CSVNodeMapping fromNodeMapping = new CSVNodeMapping(
                "FromNodeLabel",
                Map.of(0, "from_node_attr"), List.of("from_node_attr"));
        CSVNodeMapping toNodeMapping = new CSVNodeMapping(
                "ToNodeLabel",
                Map.of(1, "to_node_attr"), List.of("to_node_attr"));
        CSVEdgeMapping edgeMapping = new CSVEdgeMapping(
                "EdgeLabel",
                Map.of(2, "edge_attr_one"),
                fromNodeMapping,
                toNodeMapping);
        CSVSchemaMapping schemaMapping = new CSVSchemaMapping();
        schemaMapping.setFromNodeMapping(fromNodeMapping);
        schemaMapping.setToNodeMapping(toNodeMapping);
        schemaMapping.setEdgeMapping(edgeMapping);

        //then
        Set<String> loadCsvCall = migrationStrategy.createMigrationQueries(schemaMapping).get(1);

        String expected =
                "CALL apoc.load.csv('file:///data.csv', {sep: '\\t', header: false}) YIELD list \n" +
                        "CALL {\n" +
                        "  WITH list\n" +
                        "  MERGE (p1:FromNodeLabel {from_node_attr: list[0]})\n" +
                        "  MERGE (p2:ToNodeLabel {to_node_attr: list[1]})\n" +
                        "  MERGE (p1)-[e:EdgeLabel {edge_attr_one: list[2]}]->(p2)\n" +
                        "} IN TRANSACTIONS OF 10000 ROWS";

        assertTrue(loadCsvCall.contains(expected));

    }
}
