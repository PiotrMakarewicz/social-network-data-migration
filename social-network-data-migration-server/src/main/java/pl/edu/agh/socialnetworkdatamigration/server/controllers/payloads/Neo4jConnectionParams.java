package pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Neo4jConnectionParams {
    private String host;
    private String user;
    private String password;
}
