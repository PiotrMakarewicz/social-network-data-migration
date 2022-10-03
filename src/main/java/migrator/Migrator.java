package migrator;

import mapping.SchemaMapping;

public interface Migrator<S extends SchemaMapping> extends AutoCloseable {
    void migrateData(S schemaMapping);
}