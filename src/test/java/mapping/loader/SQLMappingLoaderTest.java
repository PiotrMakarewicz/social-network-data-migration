package mapping.loader;

import com.google.gson.Gson;
import mapping.SchemaMapping;
import mapping.loader.json.JsonSchema;
import mapping.node.SQLNodeMapping;
import org.junit.jupiter.api.Test;

import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;

public class SQLMappingLoaderTest {

    @Test
    void test(){
        String rawJson = """
            {
              "nodes": [
                {
                  "sqlTableName": "authors",
                  "nodeLabel": "Person",
                  "mappedColumns": {
                    "id": "id",
                    "bloglink": "blog_url",
                    "name": "name"
                  }
                }
              ],
              "edges": [
                {
                  "edgeLabel": "IsAuthor",
                  "foreignKey": "comments.author_id",
                  "from": "authors",
                  "to": "comments"
                },
                {
                  "edgeLabel": "RefersTo",
                  "foreignKey": "comments.post_id",
                  "from": "comments",
                  "to": "posts"
                },
                {
                  "edgeLabel": "IsTaggedWith",
                  "joinTable": "posts_tags",
                  "from": "posts",
                  "to": "tags",
                  "mappedColumns": {
                    "timestamp": "timestamp"
                  }
                }
              ]
            }
        """;

        JsonSchema jsonSchema = new Gson().fromJson(rawJson, JsonSchema.class);
        var loader = new SQLMappingLoader();
        SchemaMapping mapping = loader.convertToSchemaMapping(jsonSchema);
        assertEquals(1, mapping.getNodeMappings().size());
        assertEquals(3, mapping.getEdgeMappings().size());
        assertTrue(mapping.getNodeMappings().stream().allMatch(
                n -> n instanceof SQLNodeMapping
                        && Objects.equals(((SQLNodeMapping) n).getSqlTableName(), "authors")
                        && Objects.equals(n.getNodeLabel(), "Person")
                )
        );

        // TODO gdy będę miał więcej czasu to napiszę lepszy test - Piotrek
    }
}
