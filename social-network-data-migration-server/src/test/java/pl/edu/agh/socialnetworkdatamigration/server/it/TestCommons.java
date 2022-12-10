package pl.edu.agh.socialnetworkdatamigration.server.it;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

public class TestCommons {
    public static final String POSTGRES_USER = "postgres";
    public static final String POSTGRES_PASSWORD = "postgres";
    public static final String POSTGRES_DBNAME = "migration_db";
    public static final Integer POSTGRES_PORT = 5432;
    public static final String NEO4J_USER = "neo4j";
    public static final String NEO4J_PASSWORD = "password";
    public static final Integer NEO4J_DBMS_PORT = 7687;
    public static final Integer NEO4J_BROWSER_HTTP_PORT = 7474;

    public static GenericContainer<?> createPostgresContainer(){
        return new PostgreSQLContainer("postgres:latest")
                .withUsername(POSTGRES_USER)
                .withPassword(POSTGRES_PASSWORD)
                .withDatabaseName(POSTGRES_DBNAME)
                .withInitScript("test-data.sql")
                .withExposedPorts(POSTGRES_PORT);
    }

    public static String getMappedHostAndPort(GenericContainer<?> container, int originalPort){
        return container.getHost() + ":" + container.getMappedPort(originalPort);
    }

    public static ObjectMapper objectMapper = new ObjectMapper();

    public static String asJsonString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }
}
