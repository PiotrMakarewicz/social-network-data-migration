package pl.edu.agh.socialnetworkdatamigration.server.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.PostgreConnectionParams;
import pl.edu.agh.socialnetworkdatamigration.server.controllers.PostgreSchemaController;

import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PostgreSchemaController.class)
public class PostgreSchemaControllerIntegrationTest {

    public static final String POSTGRES_USER = "postgres";
    public static final String POSTGRES_PASSWORD = "postgres";
    public static final String POSTGRES_DBNAME = "migration_db";
    public static final Integer POSTGRES_PORT = 5432;

    @Rule
    public static final GenericContainer postgresqlContainer =
            new PostgreSQLContainer("postgres:latest")
                    .withUsername(POSTGRES_USER)
                    .withPassword(POSTGRES_PASSWORD)
                    .withDatabaseName(POSTGRES_DBNAME)
                    .withExposedPorts(POSTGRES_PORT);

    @Autowired
    private MockMvc mvc;

    @Test
    public void postgreSchemaEndpointReturns200() throws Exception {
        mvc.perform( MockMvcRequestBuilders
                        .post("/employees")
                        .content(asJsonString(new PostgreConnectionParams(postgresqlContainer.getHost(), POSTGRES_DBNAME, POSTGRES_USER, POSTGRES_PASSWORD)))
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());
    }

    public static String asJsonString(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
