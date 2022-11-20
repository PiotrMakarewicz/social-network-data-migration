package pl.edu.agh.socialnetworkdatamigration.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.socialnetworkdatamigration.server.MigrationRegistry;

import java.util.concurrent.CompletableFuture;

@Service
public class CommonMigrationService {
    private final MigrationRegistry registry;

    CommonMigrationService(@Autowired MigrationRegistry registry) {
        this.registry = registry;
    }

    public int startMigration(Runnable runnable){
        var migrationId = registry.registerNewMigration();
        CompletableFuture.runAsync(runnable)
                .thenAccept((a) -> registry.markMigrationSucceeded(migrationId))
                .exceptionally((exc) -> {
                    registry.markMigrationFailed(migrationId, exc);
                    exc.printStackTrace(System.err);
                    return null;
                });
        return migrationId;
    }
}
