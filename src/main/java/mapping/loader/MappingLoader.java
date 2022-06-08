package mapping.loader;

import mapping.SchemaMapping;

import java.io.IOException;

public interface MappingLoader {
    SchemaMapping load(String filename) throws IOException;
}
