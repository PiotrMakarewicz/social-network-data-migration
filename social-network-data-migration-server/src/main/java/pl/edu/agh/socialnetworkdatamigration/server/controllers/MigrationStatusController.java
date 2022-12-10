package pl.edu.agh.socialnetworkdatamigration.server.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;
import pl.edu.agh.socialnetworkdatamigration.server.MigrationRegistry;
import pl.edu.agh.socialnetworkdatamigration.server.domain.MigrationStatus;

import java.util.Optional;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class MigrationStatusController {
    private final MigrationRegistry registry;

    public MigrationStatusController(@Autowired MigrationRegistry registry){
        this.registry = registry;
    }

    @RequestMapping(value = "migration_status/{migrationId}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getMigrationStatus(@PathVariable int migrationId){
        Optional<MigrationStatus> status = registry.getMigrationStatus(migrationId);
        return status.orElseThrow(
                () -> new ResponseStatusException(NOT_FOUND, "There is no migration with id " + migrationId)
        ).toString();
    }

    @RequestMapping(value = "migration_failure_reason/{migrationId}", method = RequestMethod.GET, produces = "text/plain")
    @ResponseBody
    public String getMigrationFailureReason(@PathVariable int migrationId){
        Optional<Throwable> reason = registry.getMigrationFailureReason(migrationId);
         return reason.orElseThrow(
                 () -> new ResponseStatusException(NOT_FOUND, "There is no failed migration with id " + migrationId)
         ).getMessage();
    }
}
