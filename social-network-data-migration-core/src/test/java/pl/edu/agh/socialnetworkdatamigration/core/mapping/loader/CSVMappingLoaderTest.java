package pl.edu.agh.socialnetworkdatamigration.core.mapping.loader;

import org.junit.jupiter.api.Test;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.CSVEdgeMapping;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class CSVMappingLoaderTest {
    Path csvInputPath = Path.of("src/test/resources/test.csv");
    Path csvWithHeadersMappingsPath = Path.of("src/test/resources/csv_with_headers.json");
    Path csvNoHeadersMappingsPath = Path.of("src/test/resources/csv_no_headers.json");
    String csvWithHeadersMappingsJson = Files.readString(csvWithHeadersMappingsPath);
    String csvNoHeadersMappingsJson = Files.readString(csvNoHeadersMappingsPath);

    public CSVMappingLoaderTest() throws IOException {
    }

    @Test
    public void testWithHeaders() throws FileNotFoundException {
        var loader = new CSVMappingLoader(csvInputPath.toString(), true);
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
        assertTrue(from.getIdentifyingFields().contains("id"));
        assertTrue(to.getIdentifyingFields().isEmpty());
    }

    @Test
    public void testNoHeaders() throws FileNotFoundException {
        var loader = new CSVMappingLoader(csvInputPath.toString(), false);
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
        assertTrue(from.getIdentifyingFields().contains("id"));
        assertTrue(to.getIdentifyingFields().isEmpty());
    }

    @Test
    void throws_exception_when_provided_nonexistent_identifying_field() throws Exception {
        // given mapping with incorrect identifyingFields
        Path incorrectMapping = Path.of("src/test/resources/csv_incorrect_identifying_fields.json");
        String incorrectMappingJson = Files.readString(incorrectMapping);

        // when trying to load incorrect mapping exception is thrown
        var loader = new CSVMappingLoader(csvInputPath.toString(), false);
        assertThrows(RuntimeException.class, () -> loader.loadFromJson(incorrectMappingJson), "Field incorrectField not present in mappedColumns");
    }
}
