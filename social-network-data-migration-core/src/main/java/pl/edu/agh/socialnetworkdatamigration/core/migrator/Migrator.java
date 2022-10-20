package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import org.neo4j.driver.Driver;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SchemaMapping;

public abstract class Migrator<S extends SchemaMapping> implements AutoCloseable {
    protected final Driver neo4jDriver;

    protected Migrator(Driver neo4jDriver) {
        this.neo4jDriver = neo4jDriver;
    }

    public abstract void migrateData(S schemaMapping);
}