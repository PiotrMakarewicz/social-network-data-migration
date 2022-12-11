package pl.edu.agh.socialnetworkdatamigration.server.services;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.migrator.CsvAddingStrategy;
import pl.edu.agh.socialnetworkdatamigration.core.migrator.Migrator;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.Neo4jConnectionParams;
import pl.edu.agh.socialnetworkdatamigration.server.exceptions.MigrationFailedException;

public class CsvMigrationService {
    private final CommonMigrationService commonMigrationService;

    CsvMigrationService(@Autowired CommonMigrationService commonMigrationService) {
        this.commonMigrationService = commonMigrationService;
    }

    public int startCsvMigration(CSVSchemaMapping csvSchemaMapping,
                                 Neo4jConnectionParams neo4jConnectionParams,
                                 String csvFileUrl,
                                 boolean withHeaders
                                 ) {
        int migrationId = commonMigrationService.startMigration(() -> {
            try (var migrator = new Migrator();
                 var neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jConnectionParams.getHost(),
                         AuthTokens.basic(neo4jConnectionParams.getUser(), neo4jConnectionParams.getPassword()))) {

                var migrationStrategy = new CsvAddingStrategy(csvFileUrl, "\\t", withHeaders);

                migrator.migrateData(csvSchemaMapping, migrationStrategy, neo4jDriver, false);
            } catch (Exception e) {
                throw new MigrationFailedException(e);
            }
        });
        return migrationId;
    }
}
