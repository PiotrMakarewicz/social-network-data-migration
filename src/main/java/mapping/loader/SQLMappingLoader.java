package mapping.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;
import mapping.SQLSchemaMapping;
import mapping.edge.ForeignKeyMapping;
import mapping.edge.JoinTableMapping;
import mapping.edge.SQLEdgeMapping;
import mapping.loader.json.SQLEdgeMappingDeserializer;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;

public class SQLMappingLoader implements MappingLoader<SQLSchemaMapping> {

    public SQLSchemaMapping load(String filename) {
        Type t = new TypeToken<SQLEdgeMapping>(){}.getType();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(t, new SQLEdgeMappingDeserializer())
                .create();

        JsonReader reader;
        try {
            reader = new JsonReader(new FileReader(filename));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        SQLSchemaMapping schemaMapping = gson.fromJson(reader, SQLSchemaMapping.class);

        validate(schemaMapping);

        setNodeLabelsInEdgeMappings(schemaMapping);

        return schemaMapping;
    }

    private void validate(SQLSchemaMapping schemaMapping) {
        schemaMapping.getNodeMappings().forEach(
                nm -> {
                    if (nm.getSqlTableName() == null || nm.getMappedColumns().isEmpty() || nm.getNodeLabel() == null)
                        throw new RuntimeException("Invalid schema mapping JSON file");
                });

        schemaMapping.getEdgeMappings().forEach(
                em -> {
                    if (em.getFromTable() == null || em.getToTable() == null || em.getEdgeLabel() == null)
                        throw new RuntimeException("Invalid schema mapping JSON file");
                    if (em instanceof ForeignKeyMapping fkm && fkm.getForeignKeyTable() == null)
                        throw new RuntimeException("Invalid schema mapping JSON file");
                    else if (em instanceof JoinTableMapping jtm && jtm.getJoinTable() == null)
                        throw new RuntimeException("Invalid schema mapping JSON file");
                });
    }

    private void setNodeLabelsInEdgeMappings(SQLSchemaMapping mapping){
        mapping.getEdgeMappings().forEach(
                em -> {
                    em.setFromNode(mapping.getNodeLabelForTableName(em.getFromTable()).get());
                    em.setToNode(mapping.getNodeLabelForTableName(em.getToTable()).get());
                }
        );
    }
}
