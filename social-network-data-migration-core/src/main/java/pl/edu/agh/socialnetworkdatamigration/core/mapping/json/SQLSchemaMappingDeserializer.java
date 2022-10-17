package pl.edu.agh.socialnetworkdatamigration.core.mapping.json;

import com.google.gson.*;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.ForeignKeyMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.JoinTableMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.SQLEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.json.exceptions.InvalidSchemaMappingJsonException;

import java.lang.reflect.Type;
import java.util.NoSuchElementException;

public class SQLSchemaMappingDeserializer implements JsonDeserializer<SQLSchemaMapping> {

    @Override
    public SQLSchemaMapping deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        SQLSchemaMapping schemaMapping = new GsonBuilder()
                .registerTypeAdapter(SQLEdgeMapping.class, new SQLEdgeMappingDeserializer())
                .create()
                .fromJson(json, SQLSchemaMapping.class);
        try {
            setNodeLabelsInEdgeMappings(schemaMapping);
            validate(schemaMapping);
        } catch (InvalidSchemaMappingJsonException e) {
            throw new JsonParseException(e);
        }
        return schemaMapping;
    }

    private void validate(SQLSchemaMapping schemaMapping) throws InvalidSchemaMappingJsonException {
        for (var nm : schemaMapping.getNodeMappings())
            if (nm.getSqlTableName() == null || nm.getMappedColumns().isEmpty() || nm.getNodeLabel() == null)
                throw new InvalidSchemaMappingJsonException();

        for (var em : schemaMapping.getEdgeMappings()) {
            if (em.getFromTable() == null || em.getToTable() == null || em.getEdgeLabel() == null)
                throw new InvalidSchemaMappingJsonException();
            if (em instanceof ForeignKeyMapping && ((ForeignKeyMapping) em).getForeignKeyTable() == null)
                throw new InvalidSchemaMappingJsonException();
            if (em instanceof JoinTableMapping && ((JoinTableMapping) em).getJoinTable() == null)
                throw new InvalidSchemaMappingJsonException();
        }
    }

    private void setNodeLabelsInEdgeMappings(SQLSchemaMapping mapping) throws InvalidSchemaMappingJsonException {
        try {
            mapping.getEdgeMappings().forEach(
                    em -> {
                        em.setFromNode(mapping.getNodeLabelForTableName(em.getFromTable()).get());
                        em.setToNode(mapping.getNodeLabelForTableName(em.getToTable()).get());
                    }
            );
        } catch (NoSuchElementException e) {
            throw new InvalidSchemaMappingJsonException();
        }
    }
}
