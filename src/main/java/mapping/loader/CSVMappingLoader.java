package mapping.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import mapping.CSVSchemaMapping;
import mapping.loader.json.CSVMappedColumnsDeserializer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static utils.CSVUtils.getHeaders;

public class CSVMappingLoader {

    private final boolean withHeaders;
    private final char fieldTerminator = '\t';
    private List<String> headers;

    public CSVMappingLoader(String csvInputPath, boolean withHeaders) {
        this.withHeaders = withHeaders;
        if (csvInputPath != null && withHeaders)
            this.headers = getHeaders(csvInputPath, fieldTerminator);
    }

    public CSVSchemaMapping load(String configPath) throws FileNotFoundException {
        Type t = new TypeToken<Map<Integer, String>>(){}.getType();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(t, new CSVMappedColumnsDeserializer(this.withHeaders, this.headers))
                .create();

        JsonReader reader = new JsonReader(new FileReader(configPath));
        try {
            return gson.fromJson(reader, CSVSchemaMapping.class);
        } catch (JsonParseException e) {
            throw new RuntimeException("Invalid schema mapping JSON file");
        }
    }
}
