package pl.edu.agh.socialnetworkdatamigration.cli.mappingloader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.stream.JsonReader;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.json.SQLSchemaMappingDeserializer;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class SQLMappingLoader implements MappingLoader<SQLSchemaMapping> {

    public SQLSchemaMapping load(String filename) {

        Gson gson = new GsonBuilder()
                .registerTypeAdapter(SQLSchemaMapping.class, new SQLSchemaMappingDeserializer())
                .create();

        JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
       return gson.fromJson(reader, SQLSchemaMapping.class);

    }
}
