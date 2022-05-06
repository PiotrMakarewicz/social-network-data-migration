package mapping.loader;

import mapping.SchemaMapping;

import java.io.FileNotFoundException;

public interface MappingLoader {
    SchemaMapping load(String filename) throws FileNotFoundException;
}
