package pl.edu.agh.socialnetworkdatamigration.server.services;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.migrator.Migrator;
import pl.edu.agh.socialnetworkdatamigration.core.migrator.PostgresAddingStrategy;
import pl.edu.agh.socialnetworkdatamigration.core.utils.SchemaMetaData;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.Neo4jConnectionParams;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.PostgreConnectionParams;
import pl.edu.agh.socialnetworkdatamigration.server.exceptions.MigrationFailedException;

@Service
public class PostgreMigrationService {
    private final CommonMigrationService commonMigrationService;
    private Migrator migrator;

    PostgreMigrationService(@Autowired CommonMigrationService commonMigrationService, @Autowired Migrator migrator) {
        this.commonMigrationService = commonMigrationService;
        this.migrator = migrator;
    }

    public int startPostgresMigration(SQLSchemaMapping sqlSchemaMapping,
                                      PostgreConnectionParams postgreConnectionParams,
                                      Neo4jConnectionParams neo4jConnectionParams) {
        int migrationId = commonMigrationService.startMigration(() -> {
            try (var schemaMetadata = new SchemaMetaData(
                         postgreConnectionParams.getHost(),
                         postgreConnectionParams.getDbname(),
                         postgreConnectionParams.getUser(),
                         postgreConnectionParams.getPassword()
                 );
                 var migrationStrategy = new PostgresAddingStrategy(schemaMetadata);
                 var neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jConnectionParams.getHost(),
                         AuthTokens.basic(neo4jConnectionParams.getUser(), neo4jConnectionParams.getPassword()))) {
                migrator.migrateData(sqlSchemaMapping, migrationStrategy, neo4jDriver, false);
            } catch (Exception e) {
                throw new MigrationFailedException(e);
            }
        });
        return migrationId;
    }
}
