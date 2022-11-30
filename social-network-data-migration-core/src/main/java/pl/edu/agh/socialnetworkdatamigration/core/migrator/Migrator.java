package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SchemaMapping;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Migrator<S extends SchemaMapping> implements AutoCloseable {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    private final MigrationStrategy<S> migrationStrategy;
    private final Driver neo4jDriver;
    private final boolean dryRun;

    public Migrator(Driver neo4jDriver, MigrationStrategy<S> migrationStrategy, boolean dryRun) {
        this.migrationStrategy = migrationStrategy;
        this.neo4jDriver = neo4jDriver;
        this.dryRun = dryRun;
    }

    public void migrateData(S schemaMapping) {
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
        neo4jDriver.close();
        executor.shutdown();
    }
}