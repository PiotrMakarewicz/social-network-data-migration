package pl.edu.agh.socialnetworkdatamigration.cli.mappingloader;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.ForeignKeyMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.JoinTableMapping;

import java.io.FileNotFoundException;
import java.util.stream.Collectors;

public class SQLMappingLoaderTest {
    String sqlMappingsPath = getClass().getClassLoader().getResource("sql_mapping.json").getPath();

    @Test
    void test() throws FileNotFoundException {
        var loader = new SQLMappingLoader();
        SQLSchemaMapping mapping = loader.load(sqlMappingsPath);

        var nodeMappings = mapping.getNodeMappings();
        var edgeMappings = mapping.getEdgeMappings();

        Assertions.assertEquals(4, nodeMappings.size());
        Assertions.assertEquals(3, edgeMappings.size());

        var joinTableMappings = edgeMappings.stream()
                                            .filter(em -> em instanceof JoinTableMapping)
                                            .map(em -> (JoinTableMapping) em)
                                            .collect(Collectors.toList());
        Assertions.assertEquals(1, joinTableMappings.size());

        var joinTableMapping = joinTableMappings.get(0);
        Assertions.assertNotNull(joinTableMapping.getMappedColumns());
        Assertions.assertNotNull(joinTableMapping.getJoinTable());
        Assertions.assertNotNull(joinTableMapping.getToTable());
        Assertions.assertNotNull(joinTableMapping.getFromTable());
        Assertions.assertNotNull(joinTableMapping.getFromNode());
        Assertions.assertNotNull(joinTableMapping.getToNode());
        Assertions.assertNotNull(joinTableMapping.getEdgeLabel());

        var foreignKeyMappings = edgeMappings.stream()
                                                         .filter(em -> em instanceof ForeignKeyMapping)
                                                         .map(em -> (ForeignKeyMapping) em)
                                                         .collect(Collectors.toList());

        Assertions.assertEquals(2, foreignKeyMappings.size());

        for (ForeignKeyMapping fkMapping: foreignKeyMappings){
            Assertions.assertNotNull(fkMapping.getForeignKeyTable());
            Assertions.assertNotNull(fkMapping.getToTable());
            Assertions.assertNotNull(fkMapping.getFromTable());
            Assertions.assertNotNull(fkMapping.getFromNode());
            Assertions.assertNotNull(fkMapping.getToNode());
            Assertions.assertNotNull(fkMapping.getEdgeLabel());
        }
   }
}
