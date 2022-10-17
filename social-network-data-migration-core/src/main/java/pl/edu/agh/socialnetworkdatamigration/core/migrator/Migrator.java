package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.SchemaMapping;

public interface Migrator<S extends SchemaMapping> extends AutoCloseable {
    void migrateData(S schemaMapping);
}