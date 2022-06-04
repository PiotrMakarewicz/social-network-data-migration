package mapping.loader;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import mapping.CSVSchemaMapping;
import mapping.SchemaMapping;
import mapping.edge.CSVEdgeMapping;
import mapping.loader.json.CSVMappingJsonSchema;
import mapping.loader.json.EdgeJson;
import mapping.loader.json.NodeJson;
import mapping.loader.json.SQLMappingJsonSchema;
import mapping.node.CSVNodeMapping;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;

public class CSVMappingLoader  {

    public SchemaMapping load(String filename) throws FileNotFoundException {
        CSVMappingJsonSchema jsonSchema = parseJson(filename);
        return convertToSchemaMapping(jsonSchema);
    }
    protected CSVMappingJsonSchema parseJson(String filename) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(filename));
        return gson.fromJson(reader, SQLMappingJsonSchema.class);
    }
    protected SchemaMapping convertToSchemaMapping(CSVMappingJsonSchema jsonSchema) {
        SchemaMapping mapping = new CSVSchemaMapping();

        NodeJson fromNode = jsonSchema.getFromNode();
        NodeJson toNode = jsonSchema.getToNode();

        for (NodeJson node: List.of(fromNode, toNode)){
            if (node.getNodeLabel() == null
                    || node.getMappedColumns() == null
                    || node.getMappedColumns().size() == 0
            ) {
                throw new RuntimeException("Invalid schema mapping JSON file");
            }
        }

        CSVNodeMapping fromNodeMapping = new CSVNodeMapping(fromNode.getNodeLabel(), fromNode.getMappedColumns());
        CSVNodeMapping toNodeMapping = new CSVNodeMapping(toNode.getNodeLabel(), toNode.getMappedColumns());

        mapping.addNodeMapping(fromNodeMapping);
        mapping.addNodeMapping(toNodeMapping);

        EdgeJson edge = jsonSchema.getEdge();

        if (edge.getEdgeLabel() == null)
            throw new RuntimeException("Invalid schema mapping JSON file");

        var mappedColumns = edge.getMappedColumns();
        if (mappedColumns == null){
            mappedColumns = new HashMap<>();
        }
        mapping.addEdgeMapping(new CSVEdgeMapping(edge.getEdgeLabel(), mappedColumns, fromNodeMapping, toNodeMapping));

        return mapping;
    }
}
