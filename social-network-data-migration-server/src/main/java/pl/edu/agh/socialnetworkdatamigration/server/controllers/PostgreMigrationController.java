package pl.edu.agh.socialnetworkdatamigration.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.SQLMappingLoader;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.PostgreMigrationRequestPayload;
import pl.edu.agh.socialnetworkdatamigration.server.services.PostgreMigrationService;

@RestController
public class PostgreMigrationController {

    private final SQLMappingLoader mappingLoader;
    private final PostgreMigrationService service;

    public PostgreMigrationController(@Autowired SQLMappingLoader mappingLoader, @Autowired PostgreMigrationService service){
        this.mappingLoader = mappingLoader;
        this.service = service;
    }

    @RequestMapping(value = "migration/postgre", method = RequestMethod.POST, produces = "text/plain")
    public String startMigration(@RequestBody PostgreMigrationRequestPayload payload){
        SQLSchemaMapping schemaMapping = mappingLoader.loadFromJson(payload.getRawSchemaMapping());
        Integer migrationId = service.startPostgresMigration(
                schemaMapping,
                payload.getPostgreConnectionParams(),
                payload.getNeo4jConnectionParams()
        );
        return migrationId.toString();
    }
}
