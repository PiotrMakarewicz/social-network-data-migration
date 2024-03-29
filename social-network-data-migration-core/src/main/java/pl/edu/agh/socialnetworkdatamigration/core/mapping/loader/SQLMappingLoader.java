package pl.edu.agh.socialnetworkdatamigration.core.mapping.loader;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.ForeignKeyMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.JoinTableMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.SQLEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.json.SQLEdgeMappingDeserializer;

import java.lang.reflect.Type;
import java.util.List;
import java.util.Optional;

public class SQLMappingLoader implements MappingLoader<SQLSchemaMapping> {

    public SQLSchemaMapping loadFromJson(String json) {
        Type t = new TypeToken<SQLEdgeMapping>(){}.getType();
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(t, new SQLEdgeMappingDeserializer())
                .create();
        SQLSchemaMapping schemaMapping = gson.fromJson(json, SQLSchemaMapping.class);
        setNodeLabelsInEdgeMappings(schemaMapping);
        validate(schemaMapping);
        return schemaMapping;
    }

    private void validate(SQLSchemaMapping schemaMapping) {
        schemaMapping.getNodeMappings().forEach(
                nm -> {
                    if (nm.getSqlTableName() == null || nm.getMappedColumns().isEmpty() || nm.getNodeLabel() == null)
                        throw new RuntimeException("Invalid schema mapping JSON file");

                    Optional.ofNullable(nm.getIdentifyingFields()).orElse(List.of())
                            .stream()
                            .filter(field -> !nm.getMappedColumns().containsValue(field))
                            .findAny()
                            .ifPresent((s) -> {
                                throw new RuntimeException(String.format("Field %s not present in mappedColumns", s));
                            });
                });

        schemaMapping.getEdgeMappings().forEach(
                em -> {
                    if (em.getFromTable() == null || em.getToTable() == null || em.getEdgeLabel() == null)
                        throw new RuntimeException("Invalid schema mapping JSON file");
                    if (em instanceof ForeignKeyMapping && ((ForeignKeyMapping) em).getForeignKeyTable() == null)
                        throw new RuntimeException("Invalid schema mapping JSON file");
                    if (em instanceof JoinTableMapping && ((JoinTableMapping) em).getJoinTable() == null)
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
