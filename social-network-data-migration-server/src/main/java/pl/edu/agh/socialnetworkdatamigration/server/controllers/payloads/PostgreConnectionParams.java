package pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostgreConnectionParams {
    private String host;
    private String dbname;
    private String user;
    private String password;
}
