package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import org.neo4j.driver.Driver;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;
import org.neo4j.driver.Session;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SchemaMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Migrator implements AutoCloseable {
    private final ExecutorService executor = Executors.newCachedThreadPool();

    public <S extends SchemaMapping> void migrateData(S schemaMapping, MigrationStrategy<S> migrationStrategy, Driver neo4jDriver, boolean dryRun) {
        List<Set<String>> operations = migrationStrategy.createMigrationQueries(schemaMapping);
        operations.forEach(queriesSet -> waitForQueries(queriesSet, neo4jDriver, dryRun));
    }

    private void waitForQueries(Set<String> queries, Driver neo4jDriver, boolean dryRun) {
        List<CompletableFuture<?>> futures = new ArrayList<>();
        queries.forEach(query -> futures.add(execute(query, neo4jDriver, dryRun)));
        try {
            CompletableFuture.allOf(futures.toArray(new CompletableFuture<?>[0])).get();
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    private CompletableFuture<List<Record>> execute(String query, Driver neo4jDriver, boolean dryRun) {
        CompletableFuture<List<Record>> future = new CompletableFuture<>();
        System.out.println(query);

        if (dryRun) {
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
    public void close() {
        executor.shutdown();
    }
}