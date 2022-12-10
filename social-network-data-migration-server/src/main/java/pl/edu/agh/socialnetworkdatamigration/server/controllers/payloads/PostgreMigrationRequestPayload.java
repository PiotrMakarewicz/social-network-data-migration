package pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PostgreMigrationRequestPayload {
    private PostgreConnectionParams postgreConnectionParams;
    private Neo4jConnectionParams neo4jConnectionParams;
    private String rawSchemaMapping;
    // we don't want to use custom jackson deserialization for SQLSchemaMapping,
    // rather our SQLMappingLoader.loadFromJson()
}
