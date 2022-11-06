package pl.edu.agh.socialnetworkdatamigration.server.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.socialnetworkdatamigration.core.utils.SchemaMetaData;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.PostgreConnectionParams;

@RestController
public class PostgreSchemaController {
    @PostMapping(value = "/postgre_schema", consumes = "application/json")
    public SchemaMetaData fetchPostgreSchema(@RequestBody PostgreConnectionParams params){
        return new SchemaMetaData(params.getHost(), params.getDbname(), params.getUser(), params.getPassword());
    }
}
