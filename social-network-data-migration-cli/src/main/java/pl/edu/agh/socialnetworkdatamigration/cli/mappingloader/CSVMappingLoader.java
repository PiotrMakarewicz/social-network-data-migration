package pl.edu.agh.socialnetworkdatamigration.cli.mappingloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.json.CSVSchemaMappingDeserializer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;

import static pl.edu.agh.socialnetworkdatamigration.core.utils.CSVUtils.getHeaders;

public class CSVMappingLoader implements MappingLoader<CSVSchemaMapping> {

    private final boolean withHeaders;
    private final char fieldTerminator = '\t';
    private List<String> headers;

    public CSVMappingLoader(String csvInputPath, boolean withHeaders) {
        this.withHeaders = withHeaders;
        this.headers = getHeaders(csvInputPath, fieldTerminator);
    }

    public CSVSchemaMapping load(String configPath) throws FileNotFoundException  {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(CSVSchemaMapping.class, new CSVSchemaMappingDeserializer(this.withHeaders, this.headers))
                .create();

        JsonReader reader = new JsonReader(new FileReader(configPath));

        return gson.fromJson(reader, CSVSchemaMapping.class);
    }
}
