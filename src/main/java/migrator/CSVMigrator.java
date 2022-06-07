package migrator;

import mapping.SchemaMapping;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class CSVMigrator implements Migrator, AutoCloseable {
    String configPath;
    String dataPath;
    Driver neo4jDriver;

    public CSVMigrator(String configPath, String dataPath) throws IOException {
        this.configPath = configPath;
        this.dataPath = dataPath;
        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));
        String neo4jHost = properties.getProperty("neo4jHost");
        String neo4jUser = properties.getProperty("neo4jUser");
        String neo4jPassword = properties.getProperty("neo4jPassword");
        this.neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword));
    }

    @Override
    public void migrateData(SchemaMapping schemaMapping)  {

    }

    @Override
    public void close() throws Exception {
        this.neo4jDriver.close();
    }
}
