import mapping.loader.MappingLoader;
import mapping.loader.SQLMappingLoader;
import migrator.Migrator;
import migrator.PostgresMigrator;

public class Loader {

    public static void main(String[] args) throws Exception {
        if (args.length != 2) {
            System.out.println("Usage: java Loader <config-path> <mapping-path>");
            return;
        }
        try (Migrator migrator = new PostgresMigrator(args[0])){
            MappingLoader mappingLoader = new SQLMappingLoader();
            migrator.migrateData(mappingLoader.load(args[1]));
        }
    }
}
