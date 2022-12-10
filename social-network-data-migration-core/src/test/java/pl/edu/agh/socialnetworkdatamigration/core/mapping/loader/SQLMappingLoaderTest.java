package pl.edu.agh.socialnetworkdatamigration.core.mapping.loader;

import org.junit.jupiter.api.Test;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.ForeignKeyMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.JoinTableMapping;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class SQLMappingLoaderTest {
    Path sqlMappingsPath = Path.of("src/test/resources/sql_mapping.json");
    String sqlMappingsJson = Files.readString(sqlMappingsPath);

    public SQLMappingLoaderTest() throws IOException {
    }

    @Test
    void test() throws FileNotFoundException {
        var loader = new SQLMappingLoader();
        SQLSchemaMapping mapping = loader.loadFromJson(sqlMappingsJson);

        var nodeMappings = mapping.getNodeMappings();
        var edgeMappings = mapping.getEdgeMappings();

        assertEquals(4, nodeMappings.size());
        assertEquals(3, edgeMappings.size());

        nodeMappings.forEach(nm -> {
            assertTrue(nm.getIdentifyingFields().contains("id"));
        });

        var joinTableMappings = edgeMappings.stream()
                .filter(em -> em instanceof JoinTableMapping)
                .map(em -> (JoinTableMapping) em)
                .collect(Collectors.toList());
        assertEquals(1, joinTableMappings.size());

        var joinTableMapping = joinTableMappings.get(0);
        assertNotNull(joinTableMapping.getMappedColumns());
        assertNotNull(joinTableMapping.getJoinTable());
        assertNotNull(joinTableMapping.getToTable());
        assertNotNull(joinTableMapping.getFromTable());
        assertNotNull(joinTableMapping.getFromNode());
        assertNotNull(joinTableMapping.getToNode());
        assertNotNull(joinTableMapping.getEdgeLabel());

        var foreignKeyMappings = edgeMappings.stream()
                                                         .filter(em -> em instanceof ForeignKeyMapping)
                                                         .map(em -> (ForeignKeyMapping) em)
                                                         .collect(Collectors.toList());

        assertEquals(2, foreignKeyMappings.size());

        for (ForeignKeyMapping fkMapping : foreignKeyMappings) {
            assertNotNull(fkMapping.getForeignKeyTable());
            assertNotNull(fkMapping.getToTable());
            assertNotNull(fkMapping.getFromTable());
            assertNotNull(fkMapping.getFromNode());
            assertNotNull(fkMapping.getToNode());
            assertNotNull(fkMapping.getEdgeLabel());
        }
    }

    @Test
    void throws_exception_when_provided_nonexistent_identifying_field() throws Exception {
        // given mapping with incorrect identifyingFields
        Path incorrectMapping = Path.of("src/test/resources/sql_mapping_incorrect_identifying_fields.json");
        String incorrectMappingJson = Files.readString(incorrectMapping);

        // when trying to load incorrect mapping exception is thrown
        var loader = new SQLMappingLoader();
        assertThrows(RuntimeException.class, () -> loader.loadFromJson(incorrectMappingJson), "Field incorrectField not present in mappedColumns");
    }
}
