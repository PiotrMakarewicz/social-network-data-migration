import mapping.SchemaMapping;
import mapping.loader.CSVMappingLoader;
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
                CSVMappingLoader mappingLoader = new CSVMappingLoader(withHeaders);
                long start = System.currentTimeMillis();
                migrator.migrateData(mappingLoader.load(args[3]));
                long end = System.currentTimeMillis();
                System.out.printf("Time taken: %s ms", end - start);
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

    private static void printUsage() {
        System.out.println("""
                Usage: java Application <config-path> <mapping-path>
                       java Application --csv <config-path> <data-path> <mapping-path> [--no-headers]
                       java Application --i <config-path>""");
    }
}
