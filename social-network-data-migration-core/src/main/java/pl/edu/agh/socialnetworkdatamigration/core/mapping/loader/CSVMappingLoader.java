package pl.edu.agh.socialnetworkdatamigration.core.mapping.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.json.CSVMappedColumnsDeserializer;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static pl.edu.agh.socialnetworkdatamigration.core.utils.CSVUtils.getHeaders;

public class CSVMappingLoader implements MappingLoader<CSVSchemaMapping> {

    private final boolean withHeaders;
    private final char fieldTerminator = '\t';
    private List<String> headers;

    public CSVMappingLoader(String csvInputPath, boolean withHeaders) {
        this.withHeaders = withHeaders;
        this.headers = getHeaders(csvInputPath, fieldTerminator);
    }

    public CSVSchemaMapping load(String configPath) {
        Type t = new TypeToken<Map<Integer, String>>(){}.getType();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(t, new CSVMappedColumnsDeserializer(this.withHeaders, this.headers))
                .create();

        JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(configPath));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        CSVSchemaMapping schemaMapping = gson.fromJson(reader, CSVSchemaMapping.class);
        List.of(schemaMapping.getFromNodeMapping(), schemaMapping.getToNodeMapping()).forEach(
                nm -> {
                    if (nm.getNodeLabel() == null || nm.getMappedColumns().isEmpty())
                        throw new RuntimeException("Invalid schema mapping JSON file");
                }
        );

        if (schemaMapping.getEdgeMapping().getEdgeLabel() == null)
            throw new RuntimeException("Invalid schema mapping JSON file");

        return schemaMapping;
    }
}
