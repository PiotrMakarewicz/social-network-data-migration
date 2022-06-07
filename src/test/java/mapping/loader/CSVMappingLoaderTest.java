package mapping.loader;

import com.google.gson.Gson;
import mapping.CSVSchemaMapping;
import mapping.SchemaMapping;
import mapping.edge.CSVEdgeMapping;
import mapping.loader.json.CSVMappingJsonSchema;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CSVMappingLoaderTest {

    @Test
    public void test(){
        String rawJson = """
                {
                    "fromNode": {
                        "nodeLabel": "Person",
                        "mappedColumns": {
                            "0": "id"
                        }
                    },
                                
                    "toNode": {
                        "nodeLabel": "Person",
                        "mappedColumns": {
                            "1": "id"
                        }
                    },
                                
                    "edge": {
                        "edgeLabel": "InteractsWith",
                        "mappedColumns": {
                            "2": "creation_date"
                        }
                    }
                }""";

        CSVMappingJsonSchema jsonSchema = new Gson().fromJson(rawJson, CSVMappingJsonSchema.class);
        var loader = new CSVMappingLoader();
        SchemaMapping mapping = loader.convertToSchemaMapping(jsonSchema);
        assertNotNull(mapping);
        assertNotNull(mapping.getEdgeMappings());
        assertNotNull(mapping.getNodeMappings());
        assertEquals(1, mapping.getEdgeMappings().size());
        assertEquals(2, mapping.getNodeMappings().size());
        assertNotNull(mapping.getEdgeMappings().stream().findFirst().get().getEdgeLabel());

        assertInstanceOf(CSVSchemaMapping.class, mapping);
        var csvMapping = (CSVSchemaMapping) mapping;

        var edgeMapping = csvMapping.getEdgeMappings().stream().findFirst().get();

        assertInstanceOf(CSVEdgeMapping.class, edgeMapping);

        var csvEdgeMapping = (CSVEdgeMapping) edgeMapping;

        var fromNode = csvEdgeMapping.getFromNodeMapping();
        var toNode = csvEdgeMapping.getToNodeMapping();
        assertNotNull(fromNode);
        assertNotNull(toNode);
        assertTrue(mapping.getNodeMappings().containsAll(List.of(fromNode, toNode)));

        assertEquals(fromNode.getNodeLabel(), "Person");
        assertEquals(toNode.getNodeLabel(), "Person");
    }
}
