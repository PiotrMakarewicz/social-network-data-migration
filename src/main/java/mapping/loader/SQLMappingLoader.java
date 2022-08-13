package mapping.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import mapping.SQLSchemaMapping;
import mapping.edge.SQLEdgeMapping;
import mapping.loader.json.SQLEdgeMappingDeserializer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;

public class SQLMappingLoader {

    public SQLSchemaMapping load(String filename) throws FileNotFoundException {
        Type t = new TypeToken<SQLEdgeMapping>(){}.getType();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(t, new SQLEdgeMappingDeserializer())
                .create();

        JsonReader reader = new JsonReader(new FileReader(filename));
        try {
            return gson.fromJson(reader, SQLSchemaMapping.class);
        } catch (JsonParseException e) {
            throw new RuntimeException("Invalid schema mapping JSON file");
        }
    }
}
