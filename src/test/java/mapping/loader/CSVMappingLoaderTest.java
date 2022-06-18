package mapping.loader;

import com.google.gson.Gson;
import mapping.CSVSchemaMapping;
import mapping.SchemaMapping;
import mapping.edge.CSVEdgeMapping;
import mapping.edge.NoHeadersCSVEdgeMapping;
import mapping.loader.json.CSVMappingJsonSchema;
import mapping.node.NoHeadersCSVNodeMapping;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CSVMappingLoaderTest {

    @Test
    public void testWithHeaders(){
        String rawJson = """
                {
                    "fromNode": {
                        "nodeLabel": "Person",
                        "mappedColumns": {
                            "person_one": "id"
                        }
                    },
                                
                    "toNode": {
                        "nodeLabel": "Person2",
                        "mappedColumns": {
                            "person_two": "id"
                        }
                    },
                                
                    "edge": {
                        "edgeLabel": "InteractsWith",
                        "mappedColumns": {
                            "interaction_date": "creation_date"
                        }
                    }
                }""";

        CSVMappingJsonSchema jsonSchema = new Gson().fromJson(rawJson, CSVMappingJsonSchema.class);
        var loader = new CSVMappingLoader(true);
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
        assertEquals(toNode.getNodeLabel(), "Person2");
    }

    @Test
    public void testNoHeaders(){
        String rawJson = """
                {
                    "fromNode": {
                        "nodeLabel": "User",
                        "mappedColumns": {
                            "0": "id"
                        }
                    },
                                
                    "toNode": {
                        "nodeLabel": "User2",
                        "mappedColumns": {
                            "1": "id"
                        }
                    },
                                
                    "edge": {
                        "edgeLabel": "PostedAtWallOf",
                        "mappedColumns": {
                            "2": "creation_date"
                        }
                    }
                }""";

        CSVMappingJsonSchema jsonSchema = new Gson().fromJson(rawJson, CSVMappingJsonSchema.class);
        var loader = new CSVMappingLoader(false);
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

        assertInstanceOf(NoHeadersCSVEdgeMapping.class, edgeMapping);

        var csvEdgeMapping = (NoHeadersCSVEdgeMapping) edgeMapping;

        var fromNode = csvEdgeMapping.getFromNodeMapping();
        var toNode = csvEdgeMapping.getToNodeMapping();
        assertNotNull(fromNode);
        assertNotNull(toNode);
        assertTrue(mapping.getNodeMappings().containsAll(List.of(fromNode, toNode)));

        assertEquals(fromNode.getNodeLabel(), "User");
        assertEquals(toNode.getNodeLabel(), "User2");

        var nodeMapping =  mapping.getNodeMappings().stream().findFirst().get();

        assertInstanceOf(NoHeadersCSVNodeMapping.class, nodeMapping);
        var nhCsvNodeMapping = (NoHeadersCSVNodeMapping) nodeMapping;

        assertTrue(nhCsvNodeMapping.getMappedColumns().containsKey(1) || nhCsvNodeMapping.getMappedColumns().containsKey(0));
    }
}
