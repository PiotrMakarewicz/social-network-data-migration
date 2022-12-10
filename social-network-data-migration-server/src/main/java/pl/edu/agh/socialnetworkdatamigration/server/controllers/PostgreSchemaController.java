package pl.edu.agh.socialnetworkdatamigration.server.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.socialnetworkdatamigration.core.utils.SchemaMetaData;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.PostgreConnectionParams;

import java.sql.SQLException;

@RestController
public class PostgreSchemaController {
    @PostMapping(value = "/postgre_schema", consumes = "application/json")
    public SchemaMetaData fetchPostgreSchema(@RequestBody PostgreConnectionParams params) throws SQLException {
        return new SchemaMetaData(params.getHost(), params.getDbname(), params.getUser(), params.getPassword());
    }
}
