package pl.edu.agh.socialnetworkdatamigration.core.mapping.loader;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.SchemaMapping;

public interface MappingLoader<S extends SchemaMapping> {
    S load(String filename);
}