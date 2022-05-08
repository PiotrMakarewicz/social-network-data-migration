package migrator;

import mapping.SchemaMapping;

public interface Migrator {
    void migrateData(SchemaMapping schemaMapping);
}
