package pl.edu.agh.socialnetworkdatamigration.cli;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.CSVSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.SchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.CSVMappingLoader;
import pl.edu.agh.socialnetworkdatamigration.cli.interactive.InteractiveCSVMappingCreator;
import pl.edu.agh.socialnetworkdatamigration.cli.interactive.InteractiveSQLMappingCreator;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.loader.SQLMappingLoader;
import pl.edu.agh.socialnetworkdatamigration.core.migrator.CSVMigrator;
import pl.edu.agh.socialnetworkdatamigration.core.migrator.PostgresMigrator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class SocialNetworkDataMigrationCLI {
    public static void main(String[] args) throws Exception {
        if (args.length == 2 && args[0].equals("--i"))
            interactiveSQL(args);
        else if (args.length == 2)
            SQLmigration(args);
        else if (args[0].equals("--csv"))
            if (args[1].equals("--i"))
                interactiveCSV(args);
            else
                CSVmigration(args);
        else
            printUsage();
    }

    private static void SQLmigration(String[] args) throws Exception {
        String configPath = args[0];
        String mappingsPath = args[1];

        var mappingLoader = new SQLMappingLoader();
        String jsonStr = Files.readString(Path.of(mappingsPath));
        var schemaMapping = mappingLoader.loadFromJson(jsonStr);

        try (var migrator = new PostgresMigrator(configPath)) {
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

        var mappingLoader = new CSVMappingLoader(csvDataPath, withHeaders);
        String jsonStr = Files.readString(Path.of(mappingsPath));
        var schemaMapping = mappingLoader.loadFromJson(jsonStr);

        try (var migrator = new CSVMigrator(configPath, csvDataPath, withHeaders)) {
            migrator.migrateData(schemaMapping);
        }
    }

    private static void interactiveSQL(String[] args) throws IOException {
        var creator = new InteractiveSQLMappingCreator(args[1]);
        SchemaMapping mapping = creator.createInteractively();
        System.out.println(mapping);
    }

    private static void interactiveCSV(String[] args) throws Exception {
        boolean withHeaders = args.length == 6 && args[4].equals("--no-headers");
        InteractiveCSVMappingCreator mappingLoader = new InteractiveCSVMappingCreator(args[3], withHeaders);
        CSVSchemaMapping mapping = mappingLoader.createInteractively();
        try (var migrator = new CSVMigrator(args[2], args[3], withHeaders)) {
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
