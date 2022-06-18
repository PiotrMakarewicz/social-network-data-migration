package mapping.loader;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import mapping.CSVSchemaMapping;
import mapping.SchemaMapping;
import mapping.edge.CSVEdgeMapping;
import mapping.edge.NoHeadersCSVEdgeMapping;
import mapping.loader.json.CSVMappingJsonSchema;
import mapping.loader.json.EdgeJson;
import mapping.loader.json.NodeJson;
import mapping.node.CSVNodeMapping;
import mapping.node.NoHeadersCSVNodeMapping;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class CSVMappingLoader {

    private final boolean withHeaders;

    public CSVMappingLoader(boolean withHeaders) {
        this.withHeaders = withHeaders;
    }

    public SchemaMapping load(String filename) throws FileNotFoundException {
        CSVMappingJsonSchema jsonSchema = parseJson(filename);
        return convertToSchemaMapping(jsonSchema);
    }

    protected CSVMappingJsonSchema parseJson(String filename) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(filename));
        return gson.fromJson(reader, CSVMappingJsonSchema.class);
    }

    protected SchemaMapping convertToSchemaMapping(CSVMappingJsonSchema jsonSchema) {
        SchemaMapping mapping = new CSVSchemaMapping();

        NodeJson fromNode = jsonSchema.getFromNode();
        NodeJson toNode = jsonSchema.getToNode();

        for (NodeJson node : List.of(fromNode, toNode))
            if (node.getNodeLabel() == null || node.getMappedColumns().isEmpty())
                throw new RuntimeException("Invalid schema mapping JSON file");

        EdgeJson edge = jsonSchema.getEdge();
        if (edge.getEdgeLabel() == null)
            throw new RuntimeException("Invalid schema mapping JSON file");

        var fromNodeLabel = fromNode.getNodeLabel();
        var fromNodeMappedColumns = fromNode.getMappedColumns();
        var toNodeLabel = toNode.getNodeLabel();
        var toNodeMappedColumns = toNode.getMappedColumns();

        var edgeLabel = edge.getEdgeLabel();
        var edgeMappedColumns = edge.getMappedColumns();

        if (withHeaders) {
            var fromNodeMapping = new CSVNodeMapping(fromNodeLabel, fromNodeMappedColumns);
            var toNodeMapping = new CSVNodeMapping(toNodeLabel, toNodeMappedColumns);
            var edgeMapping = new CSVEdgeMapping(edgeLabel, edgeMappedColumns, fromNodeMapping, toNodeMapping);

            mapping.addNodeMapping(fromNodeMapping);
            mapping.addNodeMapping(toNodeMapping);
            mapping.addEdgeMapping(edgeMapping);
        } else {
            var fromNodeMapping = new NoHeadersCSVNodeMapping(fromNodeLabel, keysToInt(fromNodeMappedColumns));
            var toNodeMapping = new NoHeadersCSVNodeMapping(toNodeLabel, keysToInt(toNodeMappedColumns));
            var edgeMapping = new NoHeadersCSVEdgeMapping(edgeLabel, keysToInt(edgeMappedColumns), fromNodeMapping, toNodeMapping);

            mapping.addNodeMapping(fromNodeMapping);
            mapping.addNodeMapping(toNodeMapping);
            mapping.addEdgeMapping(edgeMapping);
        }

        return mapping;
    }

    private Map<Integer, String> keysToInt(Map<String, String> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(e -> Integer.parseInt(e.getKey()), Map.Entry::getValue));
    }
}
