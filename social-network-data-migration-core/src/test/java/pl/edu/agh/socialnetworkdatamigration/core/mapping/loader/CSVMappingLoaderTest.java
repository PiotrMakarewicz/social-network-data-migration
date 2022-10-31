package pl.edu.agh.socialnetworkdatamigration.core.mapping.loader;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.CSVEdgeMapping;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CSVMappingLoaderTest {
    String csvInputPath = getClass().getClassLoader().getResource("test.csv").getPath();
    String csvWithHeadersMappingsPath = getClass().getClassLoader().getResource("csv_with_headers.json").getPath();
    String csvWithHeadersMappingsJson = Files.readString(Path.of(csvWithHeadersMappingsPath));
    String csvNoHeadersMappingsPath = getClass().getClassLoader().getResource("csv_no_headers.json").getPath();

    String csvNoHeadersMappingsJson = Files.readString(Path.of(csvNoHeadersMappingsPath));

    public CSVMappingLoaderTest() throws IOException {}

    @Test
    public void testWithHeaders() throws FileNotFoundException {
        var loader = new CSVMappingLoader(csvInputPath, true);
        CSVSchemaMapping mapping = loader.loadFromJson(csvWithHeadersMappingsJson);
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
        CSVSchemaMapping mapping = loader.loadFromJson(csvNoHeadersMappingsJson);
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
