package pl.edu.agh.socialnetworkdatamigration.server.it;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.PostgreSchemaController;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.payloads.PostgreConnectionParams;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static pl.edu.agh.socialnetworkdatamigration.server.it.TestCommons.*;

@WebMvcTest(PostgreSchemaController.class)
@Testcontainers
public class PostgreSchemaControllerIntegrationTest {
    @Container
    public static final GenericContainer postgresqlContainer = createPostgresContainer();

    @Autowired
    private MockMvc mvc;

    @Test
    public void postgreSchemaEndpointReturns200() throws Exception {
        var postgreHostAndPort = getMappedHostAndPort(postgresqlContainer, POSTGRES_PORT);
        var dbConnectionParams = new PostgreConnectionParams(postgreHostAndPort, POSTGRES_DBNAME, POSTGRES_USER, POSTGRES_PASSWORD);
        var requestPayload = asJsonString(dbConnectionParams);

        mvc.perform(MockMvcRequestBuilders
                        .post("/postgre_schema")
                        .content(requestPayload)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }
}
