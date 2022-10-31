package pl.edu.agh.socialnetworkdatamigration.core.mapping.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.json.CSVMappedColumnsDeserializer;

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

    public CSVSchemaMapping loadFromJson(String jsonStr) {
        Type t = new TypeToken<Map<Integer, String>>(){}.getType();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(t, new CSVMappedColumnsDeserializer(this.withHeaders, this.headers))
                .create();

        CSVSchemaMapping schemaMapping = gson.fromJson(jsonStr, CSVSchemaMapping.class);
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
