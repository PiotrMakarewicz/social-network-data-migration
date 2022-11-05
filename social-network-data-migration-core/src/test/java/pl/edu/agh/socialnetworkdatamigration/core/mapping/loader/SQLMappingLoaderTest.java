package pl.edu.agh.socialnetworkdatamigration.core.mapping.loader;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.ForeignKeyMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.JoinTableMapping;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SQLMappingLoaderTest {
    String sqlMappingsPath = getClass().getClassLoader().getResource("sql_mapping.json").getPath();
    String sqlMappingsJson = Files.readString(Path.of(sqlMappingsPath));

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

        for (ForeignKeyMapping fkMapping: foreignKeyMappings){
            assertNotNull(fkMapping.getForeignKeyTable());
            assertNotNull(fkMapping.getToTable());
            assertNotNull(fkMapping.getFromTable());
            assertNotNull(fkMapping.getFromNode());
            assertNotNull(fkMapping.getToNode());
            assertNotNull(fkMapping.getEdgeLabel());
        }
   }
}
