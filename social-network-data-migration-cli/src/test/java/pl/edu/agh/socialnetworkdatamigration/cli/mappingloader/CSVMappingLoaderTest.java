package pl.edu.agh.socialnetworkdatamigration.cli.mappingloader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.CSVEdgeMapping;

import java.io.FileNotFoundException;

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

        Assertions.assertNotNull(from);
        Assertions.assertNotNull(to);
        Assertions.assertNotNull(edge);
        Assertions.assertNotNull(edge.getEdgeLabel());

        Assertions.assertInstanceOf(CSVSchemaMapping.class, mapping);
        Assertions.assertInstanceOf(CSVEdgeMapping.class, edge);

        Assertions.assertEquals(from.getNodeLabel(), "Person");
        Assertions.assertEquals(to.getNodeLabel(), "Person2");

        Assertions.assertTrue(from.getMappedColumns().containsKey(0));
        Assertions.assertTrue(to.getMappedColumns().containsKey(1));
        Assertions.assertTrue(edge.getMappedColumns().containsKey(2));
    }

    @Test
    public void testNoHeaders() throws FileNotFoundException {
        var loader = new CSVMappingLoader(csvInputPath, false);
        CSVSchemaMapping mapping = loader.load(csvNoHeadersMappingsPath);
        var from = mapping.getFromNodeMapping();
        var to = mapping.getToNodeMapping();
        var edge = mapping.getEdgeMapping();

        Assertions.assertNotNull(from);
        Assertions.assertNotNull(to);
        Assertions.assertNotNull(edge);
        Assertions.assertNotNull(edge.getEdgeLabel());

        Assertions.assertInstanceOf(CSVSchemaMapping.class, mapping);
        Assertions.assertInstanceOf(CSVEdgeMapping.class, edge);

        Assertions.assertEquals(from.getNodeLabel(), "User");
        Assertions.assertEquals(to.getNodeLabel(), "User2");

        Assertions.assertTrue(from.getMappedColumns().containsKey(0));
        Assertions.assertTrue(to.getMappedColumns().containsKey(1));
        Assertions.assertTrue(edge.getMappedColumns().containsKey(2));
    }
}
