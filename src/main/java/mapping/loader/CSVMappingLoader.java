package mapping.loader;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import mapping.CSVSchemaMapping;
import mapping.SchemaMapping;
import mapping.edge.CSVEdgeMapping;
import mapping.loader.json.CSVMappingJsonSchema;
import mapping.loader.json.EdgeJson;
import mapping.loader.json.NodeJson;
import mapping.node.CSVNodeMapping;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import static utils.CSVUtils.*;

public class CSVMappingLoader {

    private final boolean withHeaders;
    private final String csvInputPath;
    private final char fieldTerminator = '\t';
    private List<String> headers;


    public CSVMappingLoader(String csvInputPath, boolean withHeaders) {
        this.withHeaders = withHeaders;
        this.csvInputPath = csvInputPath;
        if (csvInputPath != null && withHeaders)
            this.headers = getHeaders(csvInputPath, fieldTerminator);
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

        CSVNodeMapping fromNodeMapping, toNodeMapping;
        CSVEdgeMapping edgeMapping;

        if (withHeaders) {
            fromNodeMapping = new CSVNodeMapping(fromNodeLabel, headersToIndexes(fromNodeMappedColumns, headers));
            toNodeMapping = new CSVNodeMapping(toNodeLabel, headersToIndexes(toNodeMappedColumns, headers));
            edgeMapping = new CSVEdgeMapping(edgeLabel, headersToIndexes(edgeMappedColumns, headers), fromNodeMapping, toNodeMapping);
        } else {
            fromNodeMapping = new CSVNodeMapping(fromNodeLabel, keysToInt(fromNodeMappedColumns));
            toNodeMapping = new CSVNodeMapping(toNodeLabel, keysToInt(toNodeMappedColumns));
            edgeMapping = new CSVEdgeMapping(edgeLabel, keysToInt(edgeMappedColumns), fromNodeMapping, toNodeMapping);
        }
        mapping.addNodeMapping(fromNodeMapping);
        mapping.addNodeMapping(toNodeMapping);
        mapping.addEdgeMapping(edgeMapping);

        return mapping;
    }
}
