package pl.edu.agh.socialnetworkdatamigration.server.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.containers.Neo4jLabsPlugin;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.ForeignKeyMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.JoinTableMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.SQLNodeMapping;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.Neo4jConnectionParams;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.PostgreConnectionParams;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.PostgreMigrationRequestPayload;
import pl.edu.agh.socialnetworkdatamigration.server.domain.MigrationStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.edu.agh.socialnetworkdatamigration.server.it.TestCommons.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
public class PostgreMigrationIntegrationTest {
    @Container
    public static final GenericContainer<?> postgresqlContainer = createPostgresContainer();

    @Container
    public static final GenericContainer<?> neo4jContainer =
            new Neo4jContainer("neo4j:latest")
                    .withAdminPassword("password")
                    .withLabsPlugins(Neo4jLabsPlugin.APOC)
                    .withExposedPorts(NEO4J_BROWSER_HTTP_PORT, NEO4J_DBMS_PORT);

    @Autowired
    private MockMvc mvc;

    @Test
    public void postgreMigrationRunsSuccessfully() throws Exception {
        var postgresHostAndPort = getMappedHostAndPort(postgresqlContainer, POSTGRES_PORT);
        var neo4jHostAndPort = getMappedHostAndPort(neo4jContainer, NEO4J_DBMS_PORT);
        var sqlSchemaMapping = new SQLSchemaMapping();
        sqlSchemaMapping.addNodeMapping(
                new SQLNodeMapping(
                        "User", "users",
                        Map.of("id", "user_id",
                                "firstname", "name"))
        );
        sqlSchemaMapping.addNodeMapping(
                new SQLNodeMapping(
                        "Post", "posts",
                        Map.of("id", "post_id",
                                "content", "text"))
        );
        sqlSchemaMapping.addEdgeMapping(
                new ForeignKeyMapping("IsAuthorOf", "User", "Post", "users", "posts", "posts"));
        sqlSchemaMapping.addEdgeMapping(
                new JoinTableMapping(
                        "Likes", "User", "Post", "users", "posts", "user_likes_post",
                        Map.of("id", "post_id",
                                "content", "text"))
        );

        var requestPayload = new PostgreMigrationRequestPayload(
                new PostgreConnectionParams(postgresHostAndPort, POSTGRES_DBNAME, POSTGRES_USER, POSTGRES_PASSWORD),
                new Neo4jConnectionParams(neo4jHostAndPort, NEO4J_USER, NEO4J_PASSWORD),
                asJsonString(sqlSchemaMapping)
        );

        MvcResult createMigrationRequestResult = mvc.perform(MockMvcRequestBuilders
                        .post("/migration/postgre")
                        .content(asJsonString(requestPayload))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String migrationId = createMigrationRequestResult.getResponse().getContentAsString();
        String migrationStatus;
        int iteration = 0;
        do {
            MvcResult getMigrationStatusResult = mvc.perform(MockMvcRequestBuilders
                            .get("/migration_status/{migrationId}", migrationId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.TEXT_PLAIN))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();
            migrationStatus = getMigrationStatusResult.getResponse().getContentAsString();
            Thread.sleep(1000);
        } while (migrationStatus.equals(MigrationStatus.STARTED.toString()) && iteration++ < 200);


        mvc.perform(MockMvcRequestBuilders
                        .get("/migration_failure_reason/{migrationId}", migrationId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.TEXT_PLAIN))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andReturn()
                .getResponse(); // waits for response

        assertEquals(migrationStatus, MigrationStatus.SUCCEEDED.toString());
    }
}
