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
        try (PostgresMigrator migrator = new PostgresMigrator(args[0])) {
            SQLMappingLoader mappingLoader = new SQLMappingLoader();
            long start = System.currentTimeMillis();
            migrator.migrateData(mappingLoader.load(args[1]));
            long end = System.currentTimeMillis();
            System.out.printf("Time taken: %s ms", end - start);
        }
    }

    private static void CSVmigration(String[] args) throws Exception {
        if ((args.length == 4 || args.length == 5 && args[4].equals("--no-headers"))) {
            boolean withHeaders = args.length == 4;
            try (var migrator = new CSVMigrator(args[1], args[2], withHeaders)) {
                CSVMappingLoader mappingLoader = new CSVMappingLoader(args[2], withHeaders);
                migrator.migrateData(mappingLoader.load(args[3]));
            }
        }
        else
            printUsage();
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