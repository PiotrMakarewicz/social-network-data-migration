package pl.edu.agh.socialnetworkdatamigration.server;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Controller {
    @PostMapping("/migrate/postgres")
    private void runMigrationFromPostgres(){
    }
}
