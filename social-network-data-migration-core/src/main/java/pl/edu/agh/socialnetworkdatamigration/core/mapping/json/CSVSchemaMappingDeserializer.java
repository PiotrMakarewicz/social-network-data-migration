package pl.edu.agh.socialnetworkdatamigration.core.mapping.json;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.json.exceptions.InvalidSchemaMappingJsonException;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

public class CSVSchemaMappingDeserializer implements JsonDeserializer<CSVSchemaMapping> {
    boolean withHeaders;
    List<String> headers;

    public CSVSchemaMappingDeserializer(boolean withHeaders, List<String> headers){
        this.withHeaders = withHeaders;
        this.headers = headers;
    }

    @Override
    public CSVSchemaMapping deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        CSVSchemaMapping schemaMapping = new GsonBuilder()
                .registerTypeAdapter(new TypeToken<Map<Integer, String>>(){}.getType(), new CSVMappedColumnsDeserializer(this.withHeaders, this.headers))
                .create()
                .fromJson(json, CSVSchemaMapping.class);

        List.of(schemaMapping.getFromNodeMapping(), schemaMapping.getToNodeMapping()).forEach(
                nm -> {
                    if (nm.getNodeLabel() == null || nm.getMappedColumns().isEmpty())
                        throw new JsonParseException(new InvalidSchemaMappingJsonException());
                }
        );

        if (schemaMapping.getEdgeMapping().getEdgeLabel() == null)
            throw new JsonParseException(new InvalidSchemaMappingJsonException());

        return schemaMapping;
    }
}
