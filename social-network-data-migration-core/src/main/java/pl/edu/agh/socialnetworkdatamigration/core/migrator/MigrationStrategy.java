package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.SchemaMapping;

import java.util.List;
import java.util.Set;

public abstract class MigrationStrategy<S extends SchemaMapping> {

    public abstract List<Set<String>> createMigrationQueries(S schemaMapping);
}
