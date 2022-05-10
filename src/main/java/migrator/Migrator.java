package migrator;

import mapping.SchemaMapping;

public interface Migrator extends AutoCloseable {
    void migrateData(SchemaMapping schemaMapping);
}
