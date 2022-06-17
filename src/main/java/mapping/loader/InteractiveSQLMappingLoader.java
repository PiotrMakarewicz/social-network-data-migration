package mapping.loader;

import mapping.SQLSchemaMapping;
import mapping.SchemaMapping;
import mapping.edge.ForeignKeyMapping;
import mapping.edge.JoinTableMapping;
import mapping.edge.SQLEdgeMapping;
import mapping.node.SQLNodeMapping;
import utils.SchemaMetaData;
import utils.info.DatabaseInfo;
import utils.info.TableInfo;

import java.io.*;
import java.util.*;

public class InteractiveSQLMappingLoader implements MappingLoader{

    private final Map<String, SQLNodeMapping> nodeMappings = new HashMap<>();
    private final Map<String, SQLEdgeMapping> edgeMappings = new HashMap<>();
    private int nodeCounter = 0;
    private int edgeCounter = 0;
    private final Console in = System.console();
    private final DatabaseInfo databaseInfo;
    private final SQLSchemaMapping schemaMapping = new SQLSchemaMapping();

    public InteractiveSQLMappingLoader(String configPath) {
        try (SchemaMetaData schemaMetaData = new SchemaMetaData(configPath)) {
            this.databaseInfo = schemaMetaData.getDatabaseInfo();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't open or close connection with database: " + e.getMessage());
        }
    }

    public SchemaMapping load(String filename) throws IOException {
        showHelp();
        do {
            System.out.print("=> ");
            String command = in.readLine();
            if (command == null) {
                System.out.println("Incorrect command");
                showHelp();
            } else if (command.matches("help")) {
                showHelp();
            } else if (command.matches("add node mapping")) {
                addNodeMapping();
            } else if (command.matches("add edge mapping")) {
                addEdgeMapping();
            } else if (command.matches("list node mappings")) {
                listNodeMappings();
            } else if (command.matches("list edge mappings")) {
                listEdgeMappings();
            } else if (command.matches("delete node mapping \\w+")) {
                String mappingName = command.split("\\s")[3];
                deleteNodeMapping(mappingName);
            } else if (command.matches("delete edge mapping \\w+")) {
                String mappingName = command.split("\\s")[3];
                deleteEdgeMapping(mappingName);
            } else if (command.matches("show table")) {
                System.out.println(databaseInfo);
            } else if (command.matches("show table \\w+")) {
                String tableName = command.split("\\s")[2];
                System.out.println(databaseInfo.tableToString(tableName));
            } else if (command.matches("list tables")) {
                listTables();
            } else if (command.matches("quit")) {
                if (questionYesNo("Are you sure?"))
                    break;
            } else {
                System.out.println("Incorrect command");
                showHelp();
            }
        } while (true);

        return schemaMapping;
    }

    private void showHelp(){
        String help = """
                List of available commands:
                help
                \tShows list of available commands.
                add node mapping
                \tAllows to create new node mapping.
                add edge mapping
                \tAllows to create new edge mapping.
                list node mappings
                \tShows detailed list of created node mappings.
                list edge mappings
                \tShows detailed list of created edge mappings.
                delete node mapping <name>
                \tDeletes node mapping.
                delete edge mapping <name>
                \tDeletes edge mapping.
                show table [name]
                \tShows detailed info about table. If no name is provided shows info about all tables in database.
                list tables
                \tShows names of tables in database.
                quit
                \tQuits interactive mapping creation and proceeds to migrate data.
                
                Use CTRL+D when asked for input to stop mapping creation.
                """;
        System.out.println(help);
    }
    private void addNodeMapping() throws IOException {
        String node;
        do {
            System.out.print("Node label: ");
            node = in.readLine();
            if (node == null) {
                if (questionYesNo("Stop mapping creation?"))
                    return;
            } else {
                break;
            }
        } while (true);

        String table;
        do {
            System.out.print("Table name: ");
            table = in.readLine();
            if (table == null) {
                if (questionYesNo("Stop mapping creation?"))
                    return;
                else
                    continue;
            }
            if (!databaseInfo.hasTable(table)) {
                System.out.printf("Table %s not found in the database.", table);
            } else {
                break;
            }
        } while (true);
        System.out.println(databaseInfo.tableToString(table));
        Map<String, String> columnMappings = createColumnMappings(table);
        SQLNodeMapping nodeMapping = new SQLNodeMapping(node, table, columnMappings);
        if (questionYesNo("Save mapping?") && !nodeMappings.containsValue(nodeMapping)) {
            String name = node + nodeCounter++;
            nodeMappings.put(name, nodeMapping);
            schemaMapping.addNodeMapping(nodeMapping);
        }
    }
    private void addEdgeMapping() throws IOException {
        String name;
        do {
            System.out.print("Edge label: ");
            name = in.readLine();
            if (name == null) {
                if (questionYesNo("Stop mapping creation?"))
                    return;
            } else {
                break;
            }
        } while (true);

        String fromTable;
        do {
            System.out.println("Source table: ");
            fromTable = in.readLine();
            if (fromTable == null) {
                if (questionYesNo("Stop mapping creation?"))
                    return;
                else
                    continue;
            }
            if (!databaseInfo.hasTable(fromTable)) {
                System.out.printf("Table %s not found in the database.", fromTable);
            } else if (schemaMapping.getNodeLabelForTableName(fromTable).isEmpty()){
                System.out.printf("Table %s exists in the database, but there is no node mapping for it.", fromTable);
            } else {
                break;
            }
        } while (true);

        String toTable;
        do {
            System.out.println("Destination table: ");
            toTable = in.readLine();
            if (toTable == null) {
                if (questionYesNo("Stop mapping creation?"))
                    return;
                else
                    continue;
            }
            if (!databaseInfo.hasTable(toTable)) {
                System.out.printf("Table %s not found in the database.", toTable);
            } else if (schemaMapping.getNodeLabelForTableName(toTable).isEmpty()){
                System.out.printf("Table %s exists in the database, but there is no node mapping for it.", toTable);
            } else {
                break;
            }
        } while (true);

        String type;
        do {
            System.out.println("Choose mapping type (foreign key/join table): ");
            type = in.readLine();
            if (type == null) {
                if (questionYesNo("Stop mapping creation?"))
                    return;
                else
                    continue;
            }
            if (!type.equals("foreign key") && !type.equals("join table")) {
                System.out.println("Incorrect mapping type.");
            } else {
                break;
            }
        } while (true);

        SQLEdgeMapping edgeMapping;

        System.out.println("Warning: tables with many foreign keys to single table currently unsupported.");
        if (type.equals("foreign key")) {
            String foreignKeyTable;
            do {
                System.out.println("Table with foreign key: ");
                foreignKeyTable = in.readLine();
                if (foreignKeyTable == null) {
                    if (questionYesNo("Stop mapping creation?"))
                        return;
                    else
                        continue;
                }
                if (!(foreignKeyTable.equals(fromTable) || foreignKeyTable.equals(toTable))) {
                    System.out.println("Foreign key table must be one of earlier chosen tables.");
                } else if (!databaseInfo.hasForeignKey(foreignKeyTable,
                        foreignKeyTable.equals(fromTable) ? toTable : fromTable)){
                    System.out.println("Table doesn't have required foreign key.");
                } else {
                    break;
                }
            } while (true);

            edgeMapping = new ForeignKeyMapping(
                    name,
                    schemaMapping.getNodeLabelForTableName(fromTable).get(),
                    schemaMapping.getNodeLabelForTableName(toTable).get(),
                    fromTable,
                    toTable,
                    foreignKeyTable
            );
        } else {
            String joinTable;
            do {
                System.out.println("Join table: ");
                joinTable = in.readLine();
                if (joinTable == null) {
                    if (questionYesNo("Stop mapping creation?"))
                        return;
                    else
                        continue;
                }
                if (!databaseInfo.hasTable(joinTable)) {
                    System.out.println("Incorrect table name");
                } else if (!databaseInfo.hasForeignKey(joinTable, fromTable)
                        || !databaseInfo.hasForeignKey(joinTable, toTable)) {
                    System.out.println("Table doesn't have required foreign key.");
                } else {
                    break;
                }
            } while (true);
            System.out.println(databaseInfo.tableToString(joinTable));
            Map<String, String> columnMappings = createColumnMappings(joinTable);
            edgeMapping = new JoinTableMapping(
                    name,
                    schemaMapping.getNodeLabelForTableName(fromTable).get(),
                    schemaMapping.getNodeLabelForTableName(toTable).get(),
                    fromTable,
                    toTable,
                    joinTable,
                    columnMappings
            );
        }
        if (questionYesNo("Save mapping?") && !edgeMappings.containsValue(edgeMapping)) {
            String mappingName = name + edgeCounter++;
            edgeMappings.put(mappingName, edgeMapping);
            schemaMapping.addEdgeMapping(edgeMapping);
        }
    }
    private void listNodeMappings(){
        System.out.println();
        for (Map.Entry<String, SQLNodeMapping> entry : nodeMappings.entrySet()) {
            System.out.printf("Mapping name:      %s%n", entry.getKey());
            System.out.println(entry.getValue());
        }
    }
    private void listEdgeMappings(){
        System.out.println();
        for (Map.Entry<String, SQLEdgeMapping> entry : edgeMappings.entrySet()) {
            System.out.printf("Mapping name:      %s%n", entry.getKey());
            System.out.println(entry.getValue());
        }
    }

    private void deleteNodeMapping(String mappingName){
        SQLNodeMapping nodeMapping = nodeMappings.remove(mappingName);
        schemaMapping.getNodeMappings().remove(nodeMapping);
    }
    private void deleteEdgeMapping(String mappingName){
        SQLEdgeMapping edgeMapping = edgeMappings.remove(mappingName);
        schemaMapping.getEdgeMappings().remove(edgeMapping);
    }


    private void listTables(){
        System.out.println();
        for (TableInfo table : databaseInfo.tables) {
            System.out.println(table.tableName);
        }
        System.out.flush();
    }

    private Map<String, String> createColumnMappings(String table) throws IOException {
        Map<String, String> columnMappings = new HashMap<>();
        if (questionYesNo("Add column mappings?")) {
            boolean stopMappingCreation = false;
            do {
                boolean stopCurrentCreation = false;
                String column;
                do {
                    System.out.print("Column name: ");
                    column = in.readLine();
                    if (column == null) {
                        if (questionYesNo("Stop column mapping creation?")) {
                            stopMappingCreation = true;
                            break;
                        } else {
                            continue;
                        }
                    }
                    if (!databaseInfo.hasColumn(table, column)) {
                        System.out.println("Incorrect column name.");
                    } else {
                        break;
                    }
                } while (true);
                if (stopMappingCreation)
                    break;
                String prop;
                do {
                    System.out.println("Node/Edge property name: ");
                    prop = in.readLine();
                    if (prop == null) {
                        if (questionYesNo("Stop current mapping creation?")) {
                            if (questionYesNo("Stop column mapping creation?"))
                                stopMappingCreation = true;
                            stopCurrentCreation = true;
                            break;
                        } else {
                            continue;
                        }
                    }
                    if (columnMappings.containsKey(prop)) {
                        System.out.println("Property name already in use.");
                    } else {
                        break;
                    }
                } while (true);
                if (!stopMappingCreation && !stopCurrentCreation)
                    columnMappings.put(column, prop);
            } while (!stopMappingCreation);
        }
        return columnMappings;
    }

    private boolean questionYesNo(String prompt){
        while (true) {
            System.out.println(prompt + " (y/n)");
            System.out.flush();
            String stop = in.readLine();
            if (stop == null)
                continue;
            if (stop.toLowerCase(Locale.ROOT).equals("y"))
                return true;
            if (stop.toLowerCase(Locale.ROOT).equals("n"))
                return false;
        }
    }
}
