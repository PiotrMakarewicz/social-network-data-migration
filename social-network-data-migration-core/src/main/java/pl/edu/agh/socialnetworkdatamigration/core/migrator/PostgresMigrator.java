package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.utils.SchemaMetaData;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PostgresMigrator extends Migrator<SQLSchemaMapping> {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final SchemaMetaData schemaMetaData;
    private boolean dryRun;

    public PostgresMigrator(Driver neo4jDriver, SchemaMetaData schemaMetaData) {
        super(neo4jDriver);
        this.schemaMetaData = schemaMetaData;
    }

    public PostgresMigrator(Driver neo4jDriver, SchemaMetaData schemaMetaData, boolean dryRun) {
        this(neo4jDriver, schemaMetaData);
        this.dryRun = dryRun;
    }

    public void migrateData(SQLSchemaMapping schemaMapping) {
        MigrationStrategy<SQLSchemaMapping> migrationStrategy = new PostgresAddingStrategy(schemaMetaData);
        List<Set<String>> operations = migrationStrategy.createMigrationQueries(schemaMapping);
        operations.forEach(this::waitForQueries);
    }

    private void waitForQueries(Set<String> queries) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        queries.forEach(query -> futures.add(execute(query)));
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<List<Record>> execute(String query) {
        CompletableFuture<List<Record>> future = new CompletableFuture<>();
        System.out.println(query);

        if (this.dryRun) {
            future.complete(null);
            return future;
        }

        executor.submit(() -> {
            try (Session session = neo4jDriver.session()) {
                Result result = session.run(query);
                future.complete(result.list());
            }
        });

        return future;
    }

    @Override
    public void close() throws SQLException {
        this.neo4jDriver.close();
        this.schemaMetaData.close();
        executor.shutdown();
    }
}
