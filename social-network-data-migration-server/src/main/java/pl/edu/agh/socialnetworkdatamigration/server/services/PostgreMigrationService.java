package pl.edu.agh.socialnetworkdatamigration.server.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.migrator.PostgresMigrator;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.Neo4jConnectionParams;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.PostgreConnectionParams;
import pl.edu.agh.socialnetworkdatamigration.server.exceptions.MigrationFailedException;

@Service
public class PostgreMigrationService {
    private final CommonMigrationService commonMigrationService;

    PostgreMigrationService(@Autowired CommonMigrationService commonMigrationService) {
        this.commonMigrationService = commonMigrationService;
    }

    public int startPostgresMigration(SQLSchemaMapping sqlSchemaMapping,
                                      PostgreConnectionParams postgreConnectionParams,
                                      Neo4jConnectionParams neo4jConnectionParams) {
        int migrationId = commonMigrationService.startMigration(() -> {
            try (var migrator = new PostgresMigrator(
                    neo4jConnectionParams.getHost(),
                    neo4jConnectionParams.getUser(),
                    neo4jConnectionParams.getPassword(),
                    postgreConnectionParams.getHost(),
                    postgreConnectionParams.getDbname(),
                    postgreConnectionParams.getUser(),
                    postgreConnectionParams.getPassword()
            )) {
                migrator.migrateData(sqlSchemaMapping);
            } catch (Exception e) {
                throw new MigrationFailedException(e);
            }
        });
        return migrationId;
    }
}
