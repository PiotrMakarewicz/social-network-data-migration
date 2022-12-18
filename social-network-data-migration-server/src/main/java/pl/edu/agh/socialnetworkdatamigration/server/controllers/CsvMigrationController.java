package pl.edu.agh.socialnetworkdatamigration.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.CSVMappingLoader;
import pl.edu.agh.socialnetworkdatamigration.core.migrator.Migrator;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.CsvMigrationRequestPayload;
import pl.edu.agh.socialnetworkdatamigration.server.services.CsvMigrationService;

@RestController
public class CsvMigrationController {
    private final CsvMigrationService service;

    public CsvMigrationController(@Autowired CsvMigrationService service, @Autowired Migrator migrator){
        this.service = service;
    }

    @RequestMapping(value = "migration/csv", method = RequestMethod.POST, produces = "text/plain")
    public String startMigration(@RequestBody CsvMigrationRequestPayload payload){
        var csvInputPath = payload.getCsvFileUrl();
        var withHeaders = payload.isWithHeaders();
        var rawSchemaMappingJson = payload.getRawSchemaMapping();
        var schemaMapping = new CSVMappingLoader(csvInputPath, withHeaders).loadFromJson(rawSchemaMappingJson);

        Integer migrationId = service.startCsvMigration(
                schemaMapping,
                payload.getNeo4jConnectionParams(),
                csvInputPath,
                withHeaders
        );
        return migrationId.toString();
    }

}
