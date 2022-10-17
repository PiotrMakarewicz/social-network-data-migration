package pl.edu.agh.socialnetworkdatamigration.cli.mappingloader;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.SchemaMapping;

import java.io.FileNotFoundException;

public interface MappingLoader<S extends SchemaMapping> {
    S load(String filename) throws FileNotFoundException;
}
