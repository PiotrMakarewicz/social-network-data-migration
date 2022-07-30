package mapping.loader;

import com.google.gson.Gson;
import mapping.SchemaMapping;
import mapping.loader.json.SQLMappingJsonSchema;
import org.junit.jupiter.api.Test;

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
                },
                {
                  "sqlTableName": "comments",
                  "nodeLabel": "Post",
                  "mappedColumns": {
                    "id": "id",
                    "content": "content",
                    "date": "timestamp"
                  }
                },
                {
                  "sqlTableName": "posts",
                  "nodeLabel": "Post",
                  "mappedColumns": {
                    "id": "id",
                    "content": "content",
                    "date": "timestamp"
                  }
                },
                {
                  "sqlTableName": "tags",
                  "nodeLabel": "Tag",
                  "mappedColumns": {
                    "id": "id",
                    "name": "name"
                  }
                }
              ],
              "edges": [
                {
                  "edgeLabel": "IsAuthor",
                  "foreignKeyTable": "comments",
                  "from": "authors",
                  "to": "comments"
                },
                {
                  "edgeLabel": "RefersTo",
                  "foreignKeyTable": "comments",
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

        SQLMappingJsonSchema jsonSchema = new Gson().fromJson(rawJson, SQLMappingJsonSchema.class);
        var loader = new SQLMappingLoader();
        SchemaMapping mapping = loader.convertToSchemaMapping(jsonSchema);
        assertEquals(4, mapping.getNodeMappings().size());
        assertEquals(3, mapping.getEdgeMappings().size());
   }
}
