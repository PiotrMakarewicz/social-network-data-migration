package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.SchemaMapping;

public abstract class Migrator<S extends SchemaMapping> implements AutoCloseable {

    protected final Neo4jQueryExecutor executor;

    protected Migrator(Neo4jQueryExecutor executor){
        this.executor = executor;
    }

    abstract void migrateData(S schemaMapping);

    @Override
    public void close() throws Exception {
        executor.close();
    }
}