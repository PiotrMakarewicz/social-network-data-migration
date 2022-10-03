package mapping.loader;

import mapping.SchemaMapping;

public interface MappingLoader<S extends SchemaMapping> {
    S load(String filename);
}
