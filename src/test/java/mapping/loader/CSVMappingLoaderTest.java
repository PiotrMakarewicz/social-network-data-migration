package mapping.loader;

import mapping.CSVSchemaMapping;
import mapping.edge.CSVEdgeMapping;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;

import static org.junit.jupiter.api.Assertions.*;

public class CSVMappingLoaderTest {
    String csvInputPath = getClass().getClassLoader().getResource("test.csv").getPath();
    String csvWithHeadersMappingsPath = getClass().getClassLoader().getResource("csv_with_headers.json").getPath();
    String csvNoHeadersMappingsPath = getClass().getClassLoader().getResource("csv_no_headers.json").getPath();

    @Test
    public void testWithHeaders() throws FileNotFoundException {
        var loader = new CSVMappingLoader(csvInputPath, true);
        CSVSchemaMapping mapping = loader.load(csvWithHeadersMappingsPath);
        var from = mapping.getFromNodeMapping();
        var to = mapping.getToNodeMapping();
        var edge = mapping.getEdgeMapping();

        assertNotNull(from);
        assertNotNull(to);
        assertNotNull(edge);
        assertNotNull(edge.getEdgeLabel());

        assertInstanceOf(CSVSchemaMapping.class, mapping);
        assertInstanceOf(CSVEdgeMapping.class, edge);

        assertEquals(from.getNodeLabel(), "Person");
        assertEquals(to.getNodeLabel(), "Person2");

        assertTrue(from.getMappedColumns().containsKey(0));
        assertTrue(to.getMappedColumns().containsKey(1));
        assertTrue(edge.getMappedColumns().containsKey(2));
    }

    @Test
    public void testNoHeaders() throws FileNotFoundException {
        var loader = new CSVMappingLoader(csvInputPath, false);
        CSVSchemaMapping mapping = loader.load(csvNoHeadersMappingsPath);
        var from = mapping.getFromNodeMapping();
        var to = mapping.getToNodeMapping();
        var edge = mapping.getEdgeMapping();

        assertNotNull(from);
        assertNotNull(to);
        assertNotNull(edge);
        assertNotNull(edge.getEdgeLabel());

        assertInstanceOf(CSVSchemaMapping.class, mapping);
        assertInstanceOf(CSVEdgeMapping.class, edge);

        assertEquals(from.getNodeLabel(), "User");
        assertEquals(to.getNodeLabel(), "User2");

        assertTrue(from.getMappedColumns().containsKey(0));
        assertTrue(to.getMappedColumns().containsKey(1));
        assertTrue(edge.getMappedColumns().containsKey(2));
    }
}
