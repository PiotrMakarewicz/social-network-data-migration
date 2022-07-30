import mapping.SchemaMapping;
import mapping.loader.CSVMappingLoader;
import mapping.loader.interactive.InteractiveCSVMappingLoader;
import mapping.loader.interactive.InteractiveSQLMappingLoader;
import mapping.loader.SQLMappingLoader;
import migrator.CSVMigrator;
import migrator.Migrator;
import migrator.PostgresMigrator;

import java.io.IOException;

public class Application {
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
        try (Migrator migrator = new PostgresMigrator(args[0])) {
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
            try (Migrator migrator = new CSVMigrator(args[1], args[2], withHeaders)) {
                CSVMappingLoader mappingLoader = new CSVMappingLoader(args[2], withHeaders);
                migrator.migrateData(mappingLoader.load(args[3]));
            }
        }
        else
            printUsage();
    }

    private static void interactiveSQL(String[] args) throws IOException {
        InteractiveSQLMappingLoader mappingLoader = new InteractiveSQLMappingLoader(args[1]);
        SchemaMapping mapping = mappingLoader.load(null);
        System.out.println(mapping);
    }

    private static void interactiveCSV(String[] args) throws Exception {
        boolean withHeaders = args.length == 6 && args[4].equals("--no-headers");
        InteractiveCSVMappingLoader mappingLoader = new InteractiveCSVMappingLoader(args[3], withHeaders);
        SchemaMapping mapping = mappingLoader.load(null);
        try (Migrator migrator = new CSVMigrator(args[2], args[3], withHeaders)) {
            migrator.migrateData(mapping);
        }
    }

    private static void printUsage() {
        System.out.println("""
                Usage: java Application <config-path> <mapping-path>
                       java Application --csv <config-path> <data-path> <mapping-path> [--no-headers]
                       java Application --i <config-path>
                       java Application --csv --i <config-path> <data-path> [--no-headers]""");
    }
}
