package pl.edu.agh.socialnetworkdatamigration.cli;

import org.apache.commons.cli.*;
import org.apache.commons.text.StringEscapeUtils;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import pl.edu.agh.socialnetworkdatamigration.cli.interactive.InteractiveCSVMappingCreator;
import pl.edu.agh.socialnetworkdatamigration.cli.interactive.InteractiveSQLMappingCreator;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.CSVMappingLoader;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.SQLMappingLoader;
import pl.edu.agh.socialnetworkdatamigration.core.migrator.*;
import pl.edu.agh.socialnetworkdatamigration.core.utils.SchemaMetaData;

import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class SocialNetworkDataMigrationCLI {
    public static void main(String[] args) throws Exception {
        CommandLine cmd = getArgs(args);

        Properties properties = new Properties();
        properties.load(new FileInputStream(cmd.getOptionValue("config")));
        String neo4jHost = properties.getProperty("neo4jHost");
        String neo4jUser = properties.getProperty("neo4jUser");
        String neo4jPassword = properties.getProperty("neo4jPassword");

        if (cmd.hasOption("csv")) {
            String dataPath = cmd.getOptionValue("csv");
            String fieldTerminator = cmd.hasOption("field-terminator") ? cmd.getOptionValue("field-terminator") : "\\t";
            boolean hasHeaders = !cmd.hasOption("no-headers");
            CSVSchemaMapping schemaMapping;
            CsvStrategy migrationStrategy;

            if (cmd.hasOption('i')) {
                char fieldTerminatorChar = StringEscapeUtils.unescapeJava(fieldTerminator).charAt(0);
                var mappingCreator = new InteractiveCSVMappingCreator(dataPath, hasHeaders, cmd.hasOption("merge"), fieldTerminatorChar);
                schemaMapping = mappingCreator.createInteractively();
            } else {
                String mappingsPath = cmd.getOptionValue("mapping");
                String jsonStr = Files.readString(Path.of(mappingsPath));
                schemaMapping = new CSVMappingLoader(dataPath, hasHeaders).loadFromJson(jsonStr);
            }

            if (cmd.hasOption("merge")) {
                migrationStrategy = new CsvMergingStrategy(dataPath, fieldTerminator, hasHeaders);
            } else {
                migrationStrategy = new CsvAddingStrategy(dataPath, fieldTerminator, hasHeaders);
            }
            try (Driver neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword));
                 Migrator migrator = new Migrator()) {
                migrator.migrateData(schemaMapping, migrationStrategy, neo4jDriver, cmd.hasOption("dry-run"));
            }
        } else {
            String postgresHost = properties.getProperty("postgresHost");
            String postgresDB = properties.getProperty("postgresDB");
            String postgresUser = properties.getProperty("postgresUser");
            String postgresPassword = properties.getProperty("postgresPassword");
            SchemaMetaData schemaMetaData = new SchemaMetaData(postgresHost, postgresDB, postgresUser, postgresPassword);
            SQLSchemaMapping schemaMapping;
            PostgresStrategy migrationStrategy;

            if (cmd.hasOption('i')) {
                schemaMapping = new InteractiveSQLMappingCreator(schemaMetaData.getDatabaseInfo(), cmd.hasOption("merge")).createInteractively();
            } else {
                String mappingsPath = cmd.getOptionValue("mapping");
                String jsonStr = Files.readString(Path.of(mappingsPath));
                schemaMapping = new SQLMappingLoader().loadFromJson(jsonStr);
            }

            if (cmd.hasOption("merge")) {
                migrationStrategy = new PostgresMergingStrategy(schemaMetaData);
            } else {
                migrationStrategy = new PostgresAddingStrategy(schemaMetaData);
            }

            try (Driver neo4jDriver = GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword));
                 Migrator migrator = new Migrator()) {
                migrator.migrateData(schemaMapping, migrationStrategy, neo4jDriver, cmd.hasOption("dry-run"));
            }
            schemaMetaData.close();
        }
    }

    private static CommandLine getArgs(String[] args) {
        Option interactive = Option.builder("i")
                .hasArg(false)
                .required(false)
                .longOpt("interactive")
                .build();

        Option csv = Option.builder()
                .hasArg(true)
                .argName("Path to CSV file")
                .required(false)
                .longOpt("csv")
                .build();

        Option config = Option.builder()
                .hasArg(true)
                .argName("Path to config file")
                .required(true)
                .longOpt("config")
                .build();

        Option mapping = Option.builder()
                .hasArg(true)
                .argName("Path to mapping file")
                .required(false)
                .longOpt("mapping")
                .build();

        Option noHeaders = Option.builder()
                .hasArg(false)
                .required(false)
                .longOpt("no-headers")
                .build();

        Option merge = Option.builder()
                .hasArg(false)
                .required(false)
                .longOpt("merge")
                .build();

        Option dryRun = Option.builder()
                .hasArg(false)
                .required(false)
                .longOpt("dry-run")
                .build();

        Options options = new Options();
        options.addOption(interactive).addOption(csv).addOption(config).addOption(mapping).
                addOption(noHeaders).addOption(merge).addOption(dryRun);
        CommandLineParser parser = new DefaultParser();
        try {
            return parser.parse(options, args);
        } catch (ParseException e) {
            System.out.println(e.getMessage());
            printUsage();
            System.exit(1);
            return null;
        }
    }

    private static void printUsage() {
        System.out.println(
                "Usage:\n" +
                        "java -jar SocialNetworkDataMigrationCLI --config <config-path> --mapping <mapping-path> [-i] [--dry-run]" +
                        " [--merge] [--csv <data-path> [--field-terminator <field-terminator>] [--no-headers]]\n"
        );
    }
}
