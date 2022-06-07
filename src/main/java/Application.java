import mapping.loader.CSVMappingLoader;
import mapping.loader.SQLMappingLoader;
import migrator.CSVMigrator;
import migrator.Migrator;
import migrator.PostgresMigrator;

public class Application {

    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            try (Migrator migrator = new PostgresMigrator(args[0])){
                SQLMappingLoader mappingLoader = new SQLMappingLoader();
                long start = System.currentTimeMillis();
                migrator.migrateData(mappingLoader.load(args[1]));
                long end = System.currentTimeMillis();
                System.out.printf("Time taken: %s ms", end - start);
            }
        }
        else if (args.length == 4){
            try (Migrator migrator = new CSVMigrator(args[1], args[2])){
                CSVMappingLoader mappingLoader = new CSVMappingLoader();
                long start = System.currentTimeMillis();
                migrator.migrateData(mappingLoader.load(args[3]));
                long end = System.currentTimeMillis();
                System.out.printf("Time taken: %s ms", end - start);
            }
        }
        else {
            System.out.println("Usage: java Application <config-path> <mapping-path>\n" +
                               "       java Application --csv <config-path> <data-path> <mapping-path>");
        }
    }
}
