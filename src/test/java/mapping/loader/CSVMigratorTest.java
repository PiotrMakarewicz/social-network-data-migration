package mapping.loader;

import mapping.edge.CSVEdgeMapping;
import mapping.node.CSVNodeMapping;
import migrator.CSVMigrator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class CSVMigratorTest {
    @Test
    void buildLoadCsvWithHeadersQuery() throws IOException {
        CSVMigrator migrator = new CSVMigrator(null, "/data.csv", false);

        //when
        CSVNodeMapping fromNodeMapping = new CSVNodeMapping(
                "FromNodeLabel",
                Map.of(0, "from_node_attr"));
        CSVNodeMapping toNodeMapping = new CSVNodeMapping(
                "ToNodeLabel",
                Map.of(1, "to_node_attr"));
        CSVEdgeMapping edgeMapping = new CSVEdgeMapping(
                "EdgeLabel",
                Map.of(2, "edge_attr_one"),
                fromNodeMapping,
                toNodeMapping);
        //then
        String actual = migrator.buildLoadCsvQuery(edgeMapping, fromNodeMapping, toNodeMapping);

        String expected =
                "LOAD CSV\n" +
                "    FROM 'file:///data.csv'\n" +
                "    AS line\n" +
                "    FIELDTERMINATOR '\\t'\n" +
                "MERGE (p1:FromNodeLabel {from_node_attr: line[0]})\n" +
                "MERGE (p2:ToNodeLabel {to_node_attr: line[1]})\n" +
                "CREATE (p1)-[e:EdgeLabel {edge_attr_one: line[2]}]->(p2)";

        assertEquals(expected, actual);
    }
}
