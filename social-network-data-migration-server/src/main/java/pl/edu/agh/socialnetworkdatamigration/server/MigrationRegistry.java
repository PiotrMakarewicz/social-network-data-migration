package pl.edu.agh.socialnetworkdatamigration.server;

import org.springframework.stereotype.Component;
import pl.edu.agh.socialnetworkdatamigration.server.domain.MigrationStatus;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class MigrationRegistry {

    private final Map<Integer, MigrationStatus> migrationStatusMap = new ConcurrentHashMap<>();
    private final Map<Integer, Throwable> migrationFailureReasonMap = new ConcurrentHashMap<>();


    public int registerNewMigration(){
        int migrationId = migrationStatusMap.size();
        migrationStatusMap.put(migrationId, MigrationStatus.STARTED);
        return migrationId;
    }

    public void markMigrationSucceeded(int migrationId){
        migrationStatusMap.put(migrationId, MigrationStatus.SUCCEEDED);
    }

    public void markMigrationFailed(int migrationId, Throwable reason){
        migrationStatusMap.put(migrationId, MigrationStatus.FAILED);
        migrationFailureReasonMap.put(migrationId, reason);
    }

    public MigrationStatus getMigrationStatus(int migrationId){
        return migrationStatusMap.get(migrationId);
    }

    public Throwable getMigrationFailureReason(int migrationId){
        return migrationFailureReasonMap.get(migrationId);
    }
}
