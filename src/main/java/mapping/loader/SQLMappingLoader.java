package mapping.loader;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import mapping.SQLSchemaMapping;
import mapping.SchemaMapping;
import mapping.edge.ForeignKeyMapping;
import mapping.edge.JoinTableMapping;
import mapping.loader.json.EdgeJson;
import mapping.loader.json.SQLMappingJsonSchema;
import mapping.loader.json.NodeJson;
import mapping.node.SQLNodeMapping;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;

public class SQLMappingLoader {

    public SchemaMapping load(String filename) throws FileNotFoundException {
        SQLMappingJsonSchema jsonSchema = parseJson(filename);
        return convertToSchemaMapping(jsonSchema);
    }
    protected SQLMappingJsonSchema parseJson(String filename) throws FileNotFoundException {
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(new FileReader(filename));
        return gson.fromJson(reader, SQLMappingJsonSchema.class);
    }

    protected SchemaMapping convertToSchemaMapping(SQLMappingJsonSchema jsonSchema){
        SQLSchemaMapping mapping = new SQLSchemaMapping();

        for (NodeJson node: jsonSchema.getNodes()){
            if (node.getNodeLabel() == null || node.getSqlTableName() == null){
                throw new RuntimeException("Invalid schema mapping JSON file");
            }

            var mappedColumns = node.getMappedColumns();
            if (mappedColumns == null)
                mappedColumns = new HashMap<>();

            mapping.addNodeMapping(new SQLNodeMapping(
                    node.getNodeLabel(),
                    node.getSqlTableName(),
                    mappedColumns
            ));
        }

        for (EdgeJson edge: jsonSchema.getEdges()){
            if (edge.getEdgeLabel() == null
                    || edge.getFrom() == null
                    || edge.getTo() == null
                    || (edge.getForeignKeyTable() == null
                        && edge.getJoinTable() == null)
            ) {
                throw new RuntimeException("Invalid schema mapping JSON file");
            }
            else if (edge.getJoinTable() != null) {
                var mappedColumns = edge.getMappedColumns();
                if (mappedColumns == null)
                    mappedColumns = new HashMap<>();

                mapping.addEdgeMapping(
                        new JoinTableMapping(
                                edge.getEdgeLabel(),
                                mapping.getNodeLabelForTableName(edge.getFrom()).get(),
                                mapping.getNodeLabelForTableName(edge.getTo()).get(),
                                edge.getFrom(),
                                edge.getTo(),
                                edge.getJoinTable(),
                                mappedColumns
                        )
                );
            }
            else {
                if (!edge.getForeignKeyTable().equals(edge.getFrom())
                    && !edge.getForeignKeyTable().equals(edge.getTo())) {
                    throw new RuntimeException("Invalid schema mapping JSON file: foreign key must be part" +
                            "of %s or %s table".formatted(edge.getFrom(), edge.getTo()));
                }

                mapping.addEdgeMapping(
                        new ForeignKeyMapping(
                                edge.getEdgeLabel(),
                                mapping.getNodeLabelForTableName(edge.getFrom()).get(),
                                mapping.getNodeLabelForTableName(edge.getTo()).get(),
                                edge.getFrom(),
                                edge.getTo(),
                                edge.getForeignKeyTable()
                        )
                );
            }
        }
        return mapping;
    }
}
