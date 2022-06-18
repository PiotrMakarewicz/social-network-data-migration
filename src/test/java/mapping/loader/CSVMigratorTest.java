package mapping.loader;

import mapping.edge.CSVEdgeMapping;
import mapping.edge.EdgeMapping;
import mapping.edge.NoHeadersCSVEdgeMapping;
import mapping.node.CSVNodeMapping;
import mapping.node.NoHeadersCSVNodeMapping;
import mapping.node.NodeMapping;
import migrator.CSVMigrator;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class CSVMigratorTest {
    @Test
    void buildLoadCsvWithHeadersQuery() throws IOException {
        //given
        CSVMigrator migrator = new CSVMigrator(null, "http://data.csv", true);

        //when
        CSVNodeMapping fromNodeMapping = new CSVNodeMapping(
                "FromNodeLabel",
                Map.of("from_node_col", "from_node_attr"));
        CSVNodeMapping toNodeMapping = new CSVNodeMapping(
                "ToNodeLabel",
                Map.of("to_node_col", "to_node_attr"));
        EdgeMapping edgeMapping = new CSVEdgeMapping(
                "EdgeLabel",
                Map.of("edge_col_one", "edge_attr_one"),
                fromNodeMapping,
                toNodeMapping);
        //then
        String actual = migrator.buildLoadCsvQuery(edgeMapping, fromNodeMapping, toNodeMapping);

        String expected = """
                LOAD CSV WITH HEADERS
                    FROM 'http://data.csv'
                    AS line
                    FIELDTERMINATOR '\\t'
                MERGE (p1:FromNodeLabel {from_node_attr: line.from_node_col})
                MERGE (p2:ToNodeLabel {to_node_attr: line.to_node_col})
                CREATE (p1)-[e:EdgeLabel {edge_attr_one: line.edge_col_one}]->(p2)
                """;

        assertEquals(expected, actual);
    }

    @Test
    void buildLoadCsvQuery() throws IOException {
        //given
        CSVMigrator migrator = new CSVMigrator(null, "http://data.csv", false);

        //when
        NoHeadersCSVNodeMapping fromNodeMapping = new NoHeadersCSVNodeMapping(
                "FromNodeLabel",
                Map.of(0, "from_node_attr"));
        NoHeadersCSVNodeMapping toNodeMapping = new NoHeadersCSVNodeMapping(
                "ToNodeLabel",
                Map.of(1, "to_node_attr"));
        NoHeadersCSVEdgeMapping edgeMapping = new NoHeadersCSVEdgeMapping(
                "EdgeLabel",
                Map.of(2, "edge_attr_one"),
                fromNodeMapping,
                toNodeMapping);
        //then
        String actual = migrator.buildLoadCsvQuery(edgeMapping, fromNodeMapping, toNodeMapping);

        String expected = """
                LOAD CSV
                    FROM 'http://data.csv'
                    AS line
                    FIELDTERMINATOR '\\t'
                MERGE (p1:FromNodeLabel {from_node_attr: line[0]})
                MERGE (p2:ToNodeLabel {to_node_attr: line[1]})
                CREATE (p1)-[e:EdgeLabel {edge_attr_one: line[2]}]->(p2)
                """;

        assertEquals(expected, actual);
    }
}
