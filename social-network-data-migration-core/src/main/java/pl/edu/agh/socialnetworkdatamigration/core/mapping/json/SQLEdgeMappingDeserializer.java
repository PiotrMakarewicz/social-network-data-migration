package pl.edu.agh.socialnetworkdatamigration.core.mapping.json;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.ForeignKeyMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.JoinTableMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.SQLEdgeMapping;

import java.lang.reflect.Type;

public class SQLEdgeMappingDeserializer implements JsonDeserializer<SQLEdgeMapping> {
    @Override
    public SQLEdgeMapping deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        ForeignKeyMapping fkMapping = context.deserialize(json, ForeignKeyMapping.class);

        if (fkMapping.getForeignKeyTable() == null)
            return context.deserialize(json, JoinTableMapping.class);

        return fkMapping;
    }
}
