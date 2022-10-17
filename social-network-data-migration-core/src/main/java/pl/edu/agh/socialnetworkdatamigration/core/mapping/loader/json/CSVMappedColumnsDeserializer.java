package pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;

import static pl.edu.agh.socialnetworkdatamigration.core.utils.CSVUtils.headersToIndexes;
import static pl.edu.agh.socialnetworkdatamigration.core.utils.CSVUtils.keysToInt;


public class CSVMappedColumnsDeserializer implements JsonDeserializer<Map<Integer, String>> {

    boolean withHeaders;
    List<String> headers;

    public CSVMappedColumnsDeserializer(boolean withHeaders, List<String> headers){
        this.withHeaders = withHeaders;
        this.headers = headers;
    }

    @Override
    public Map<Integer, String> deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Type t = new TypeToken<Map<String, String>>(){}.getType();
        Map<String, String> map = context.deserialize(json, t);
        if (withHeaders)
            return headersToIndexes(map, headers);
        else
            return keysToInt(map);
    }
}
