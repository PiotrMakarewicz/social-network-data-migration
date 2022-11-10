package pl.edu.agh.socialnetworkdatamigration.cli.interactive;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.ForeignKeyMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.JoinTableMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.SQLEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.SQLNodeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.utils.info.DatabaseInfo;
import pl.edu.agh.socialnetworkdatamigration.core.utils.info.TableInfo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.*;

public class InteractiveSQLMappingCreator {
    private final Map<String, SQLNodeMapping> nodeMappings = new HashMap<>();
    private final Map<String, SQLEdgeMapping> edgeMappings = new HashMap<>();
    private int nodeCounter = 0;
    private int edgeCounter = 0;
    private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    private final DatabaseInfo databaseInfo;
    private final SQLSchemaMapping schemaMapping = new SQLSchemaMapping();

    public InteractiveSQLMappingCreator(DatabaseInfo databaseInfo) {
        this.databaseInfo = databaseInfo;
    }

    public SQLSchemaMapping createInteractively() throws IOException {
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
        String help =
                "List of available commands:\n" +
                "help\n" +
                "\tShows list of available commands.\n" +
                "add node mapping\n" +
                "\tAllows to create new node mapping.\n" +
                "add edge mapping\n" +
                "\tAllows to create new edge mapping.\n" +
                "list node mappings\n" +
                "\tShows detailed list of created node mappings.\n" +
                "list edge mappings\n" +
                "\tShows detailed list of created edge mappings.\n" +
                "delete node mapping <name>\n" +
                "\tDeletes node mapping.\n" +
                "delete edge mapping <name>\n" +
                "\tDeletes edge mapping.\n" +
                "show table [name]\n" +
                "\tShows detailed info about table. If no name is provided shows info about all tables in database.\n" +
                "list tables\n" +
                "\tShows names of tables in database.\n" +
                "quit\n" +
                "\tQuits interactive mapping creation and proceeds to migrate data.\n\n" +
                "Use ENTER when asked for input to stop mapping creation.";
        System.out.println(help);
    }
    private void addNodeMapping() throws IOException {
        String node;
        do {
            System.out.print("Node label: ");
            node = in.readLine();
            if (node.isBlank()) {
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
            if (table.isBlank()) {
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
        List<String> identifyingFields = chooseIdentifyingFields(columnMappings);
        SQLNodeMapping nodeMapping = new SQLNodeMapping(node, table, columnMappings, identifyingFields);
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
            if (name.isBlank()) {
                if (questionYesNo("Stop mapping creation?"))
                    return;
            } else {
                break;
            }
        } while (true);

        // TODO wspólna metoda
        String fromTable;
        do {
            System.out.println("Source table: ");
            fromTable = in.readLine();
            if (fromTable.isBlank()) {
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
            if (toTable.isBlank()) {
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
            if (type.isBlank()) {
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
                if (foreignKeyTable.isBlank()) {
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
                if (joinTable.isBlank()) {
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
                    if (column.isBlank()) {
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
                    if (prop.isBlank()) {
                        if (questionYesNo("Stop current mapping creation?")) {
                            if (questionYesNo("Stop column mapping creation?"))
                                stopMappingCreation = true;
                            stopCurrentCreation = true;
                            break;
                        } else {
                            continue;
                        }
                    }
                    if (columnMappings.containsValue(prop)) {
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

    private List<String> chooseIdentifyingFields(Map<String, String> mappedColumns) throws IOException {
        List<String> identifyingFields = new ArrayList<>();
        do {
            System.out.print("Field name: ");
            String field = in.readLine();
            if (field.isBlank()) {
                if (questionYesNo("Stop adding identifying fields?")) {
                    break;
                } else {
                    continue;
                }
            }
            if (!mappedColumns.containsValue(field)) {
                System.out.println("No such field.");
            } else {
                identifyingFields.add(field);
            }
        } while (true);
        return identifyingFields;
    }

    private boolean questionYesNo(String prompt) throws IOException {
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
