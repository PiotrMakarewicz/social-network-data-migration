package pl.edu.agh.socialnetworkdatamigration.cli;

import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import pl.edu.agh.socialnetworkdatamigration.cli.interactive.InteractiveCSVMappingCreator;
import pl.edu.agh.socialnetworkdatamigration.cli.interactive.InteractiveSQLMappingCreator;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.CSVMappingLoader;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.SQLMappingLoader;
import pl.edu.agh.socialnetworkdatamigration.core.migrator.CSVMigrator;
import pl.edu.agh.socialnetworkdatamigration.core.migrator.PostgresMigrator;
import pl.edu.agh.socialnetworkdatamigration.core.utils.SchemaMetaData;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.Properties;

public class SocialNetworkDataMigrationCLI {
    public static void main(String[] args) throws Exception {
        if (args.length == 2 && args[0].equals("--i")) {
            interactiveSQL(args);
        } else if (args.length == 2) {
            SQLmigration(args);
        } else if (args[0].equals("--csv")) {
            if (args[1].equals("--i")) {
                interactiveCSV(args);
            } else {
                CSVmigration(args);
            }
        } else {
            printUsage();
        }
    }

    private static void SQLmigration(String[] args) throws Exception {
        String configPath = args[0];
        String mappingsPath = args[1];

        var mappingLoader = new SQLMappingLoader();
        String jsonStr = Files.readString(Path.of(mappingsPath));
        var schemaMapping = mappingLoader.loadFromJson(jsonStr);

        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));
        String postgresHost = properties.getProperty("postgresHost");
        String postgresDB = properties.getProperty("postgresDB");
        String postgresUser = properties.getProperty("postgresUser");
        String postgresPassword = properties.getProperty("postgresPassword");
        String neo4jHost = properties.getProperty("neo4jHost");
        String neo4jUser = properties.getProperty("neo4jUser");
        String neo4jPassword = properties.getProperty("neo4jPassword");

        try (Driver neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword));
             SchemaMetaData schemaMetaData = new SchemaMetaData(postgresHost, postgresDB, postgresUser, postgresPassword);
             PostgresMigrator migrator = new PostgresMigrator(neo4jDriver, schemaMetaData)) {
            migrator.migrateData(schemaMapping);
        }
    }

    private static void CSVmigration(String[] args) throws Exception {
        boolean withHeaders = args.length == 4;
        boolean noHeaders = args.length == 5 && args[4].equals("--no-headers");

        if (!withHeaders && !noHeaders) {
            printUsage();
            return;
        }

        String configPath = args[1];
        String csvDataPath = args[2];
        String mappingsPath = args[3];

        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));
        String neo4jHost = properties.getProperty("neo4jHost");
        String neo4jUser = properties.getProperty("neo4jUser");
        String neo4jPassword = properties.getProperty("neo4jPassword");

        var mappingLoader = new CSVMappingLoader(csvDataPath, withHeaders);
        String jsonStr = Files.readString(Path.of(mappingsPath));
        var schemaMapping = mappingLoader.loadFromJson(jsonStr);


        try (Driver neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword));
             var migrator = new CSVMigrator(neo4jDriver, csvDataPath, withHeaders)) {
            migrator.migrateData(schemaMapping);
        }
    }

    private static void interactiveSQL(String[] args) throws IOException {
        String configPath = args[1];

        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));
        String postgresHost = properties.getProperty("postgresHost");
        String postgresDB = properties.getProperty("postgresDB");
        String postgresUser = properties.getProperty("postgresUser");
        String postgresPassword = properties.getProperty("postgresPassword");
        String neo4jHost = properties.getProperty("neo4jHost");
        String neo4jUser = properties.getProperty("neo4jUser");
        String neo4jPassword = properties.getProperty("neo4jPassword");

        try (Driver neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword));
             SchemaMetaData schemaMetaData = new SchemaMetaData(postgresHost, postgresDB, postgresUser, postgresPassword);
             PostgresMigrator migrator = new PostgresMigrator(neo4jDriver, schemaMetaData)) {
            var creator = new InteractiveSQLMappingCreator(schemaMetaData.getDatabaseInfo());
            SQLSchemaMapping mapping = creator.createInteractively();
            long start = System.currentTimeMillis();
            migrator.migrateData(mapping);
            long end = System.currentTimeMillis();
            System.out.printf("Time taken: %s ms", end - start);
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't establish connection with Postgres database: " + e);
        }
    }

    private static void interactiveCSV(String[] args) throws Exception {
        String configPath = args[2];

        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));
        String neo4jHost = properties.getProperty("neo4jHost");
        String neo4jUser = properties.getProperty("neo4jUser");
        String neo4jPassword = properties.getProperty("neo4jPassword");

        boolean withHeaders = args.length == 6 && args[4].equals("--no-headers");
        InteractiveCSVMappingCreator mappingLoader = new InteractiveCSVMappingCreator(args[3], withHeaders);
        CSVSchemaMapping mapping = mappingLoader.createInteractively();
        try (Driver neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword));
             var migrator = new CSVMigrator(neo4jDriver, args[3], withHeaders)) {
            migrator.migrateData(mapping);
        }
    }

    private static void printUsage() {
        System.out.println(
                "Usage: java -jar SocialNetworkDataMigrationCLI <config-path> <mapping-path>\n" +
                "       java -jar SocialNetworkDataMigrationCLI --csv <config-path> <data-path> <mapping-path> [--no-headers]\n" +
                "       java -jar SocialNetworkDataMigrationCLI --i <config-path>\n" +
                "       java -jar SocialNetworkDataMigrationCLI --csv --i <config-path> <data-path> [--no-headers]");
    }
}
