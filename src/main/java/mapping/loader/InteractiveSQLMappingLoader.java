package mapping.loader;

import mapping.SQLSchemaMapping;
import mapping.SchemaMapping;
import mapping.edge.ForeignKeyMapping;
import mapping.edge.JoinTableMapping;
import mapping.edge.SQLEdgeMapping;
import mapping.node.SQLNodeMapping;
import utils.SchemaMetaData;
import utils.SchemaMetaData.TableInfo;
import utils.SchemaMetaData.ColumnInfo;
import utils.SchemaMetaData.ForeignKeyInfo;

import java.io.*;
import java.util.*;

public class InteractiveSQLMappingLoader implements MappingLoader{

    private final Map<String, SQLNodeMapping> nodeMappings = new HashMap<>();
    private final Map<String, SQLEdgeMapping> edgeMappings = new HashMap<>();
    private int nodeCounter = 0;
    private int edgeCounter = 0;
    private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    private final List<TableInfo> tables;
    private final SQLSchemaMapping schemaMapping = new SQLSchemaMapping();

    public InteractiveSQLMappingLoader(String configPath) {
        try (SchemaMetaData schemaMetaData = new SchemaMetaData(configPath)) {
            this.tables = schemaMetaData.getTables();
        } catch (Exception e) {
            throw new RuntimeException("Couldn't open or close connection with database: " + e.getMessage());
        }
    }

    public SchemaMapping load(String filename) throws IOException {
        showHelp();
        do {
            System.out.print("=> ");
            String command = in.readLine();
            if (command == null) {
                showHelp();
            }
            if (command.matches("help")) {
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
                showTables();
            } else if (command.matches("show table \\w+")) {
                String tableName = command.split("\\s")[2];
                showTable(tableName);
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
        System.out.print("Node label: ");
        String node = in.readLine();
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
            if (!isTableCorrect(table)) {
                System.out.println("Incorrect table name");
            } else {
                break;
            }
        } while (true);
        showTable(table);
        Map<String, String> columnMappings = createColumnMappings(table);
        SQLNodeMapping nodeMapping = new SQLNodeMapping(node, table, columnMappings);
        if (questionYesNo("Save mapping?") && !nodeMappings.containsValue(nodeMapping)) {
            String name = node + nodeCounter++;
            nodeMappings.put(name, nodeMapping);
            schemaMapping.addNodeMapping(nodeMapping);
        }
    }
    private void addEdgeMapping() throws IOException {
        System.out.println("Edge label: ");
        String name = in.readLine();

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
            if (!isTableCorrect(fromTable) || schemaMapping.getNodeLabelForTableName(fromTable).isEmpty()) {
                System.out.println("Incorrect table name.");
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
            if (!isTableCorrect(toTable) || schemaMapping.getNodeLabelForTableName(toTable).isEmpty()) {
                System.out.println("Incorrect table name.");
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
                } else if (!checkForeignKeyExistence(foreignKeyTable,
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
                if (!isTableCorrect(joinTable)) {
                    System.out.println("Incorrect table name");
                } else if (!checkForeignKeyExistence(joinTable, fromTable)
                        || !checkForeignKeyExistence(joinTable, toTable)) {
                    System.out.println("Table doesn't have required foreign key.");
                } else {
                    break;
                }
            } while (true);
            showTable(joinTable);
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
            System.out.println("Mapping name:      %s".formatted(entry.getKey()));
            System.out.println(entry.getValue());
        }
    }
    private void listEdgeMappings(){
        System.out.println();
        for (Map.Entry<String, SQLEdgeMapping> entry : edgeMappings.entrySet()) {
            System.out.println("Mapping name:      %s".formatted(entry.getKey()));
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

    private void showTables() {
        tables.forEach(table -> showTable(table.tableName));
    }
    private void showTable(String tableName){
        TableInfo table = tables.stream()
                .filter(tableInfo -> tableInfo.tableName.equals(tableName))
                .findFirst()
                .orElse(null);
        if (table == null) {
            System.out.println("No such table");
            return;
        }
        int rowLen = maxRowLength();
        String roof = "-".repeat(rowLen);
        String namePaddingLeft = " ".repeat((rowLen - tableName.length() - "|".length() * 2) / 2);
        String namePaddingRight = namePaddingLeft;
        if ((rowLen - tableName.length()) % 2 == 1)
            namePaddingRight += " ";

        StringBuilder tableBuilder = new StringBuilder();

        String header = """
                
                %s
                |%s%s%s|
                %s
                """.formatted(roof, namePaddingLeft, tableName, namePaddingRight, roof);
        tableBuilder.append(header);

        for (ColumnInfo column : table.columns) {

            String columnName = column.columnName;
            String columnType = column.type;

            String columnNamePadding = " ".repeat((maxColumnNameLength() - columnName.length()));
            String columnTypePadding = " ".repeat((maxColumnTypeLength() - columnType.length()));

            String foreignColumnName = "";
            if (table.getForeignKeyInfoForColumn(column).isPresent()) {
                ForeignKeyInfo foreignKeyInfo = table.getForeignKeyInfoForColumn(column).get();
                foreignColumnName = foreignKeyInfo.referencedTableName() + "." + foreignKeyInfo.referencedColumnName();
            }
            String foreignColumnPadding = " ".repeat((maxForeignColumnLength() - foreignColumnName.length()));

            String row = """
                    | %s%s | %s%s | %s%s |
                    """.formatted(
                        column.columnName,
                        columnNamePadding,
                        column.type,
                        columnTypePadding,
                        foreignColumnName,
                        foreignColumnPadding
                    );
            tableBuilder.append(row);
        }
        tableBuilder.append(roof);
        tableBuilder.append("\n");
        System.out.print(tableBuilder);
        System.out.flush();
    }
    private void listTables(){
        System.out.println();
        for (TableInfo table : tables) {
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
                    if (!isColumnCorrect(table, column)) {
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

    private boolean isTableCorrect(String tableName) {
        return tables
                .stream()
                .map(tableInfo -> tableInfo.tableName)
                .anyMatch(table ->table.equals(tableName));
    }

    private boolean checkForeignKeyExistence(String fromTable, String toTable) {
        return tables
                .stream()
                .filter(tableInfo -> tableInfo.tableName.equals(fromTable))
                .flatMap(tableInfo -> tableInfo.foreignKeyColumns.stream())
                .anyMatch(foreignKeyInfo -> foreignKeyInfo.referencedTableName().equals(toTable));
    }

    private boolean isColumnCorrect(String tableName, String columnName) {
        return tables
                .stream()
                .filter(tableInfo -> tableInfo.tableName.equals(tableName))
                .flatMap(tableInfo -> tableInfo.columns.stream())
                .map(columnInfo -> columnInfo.columnName)
                .anyMatch(c -> c.equals(columnName));
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

    private int maxRowLength() {
        return "| ".length()
                + maxColumnNameLength()
                + " | ".length()
                + maxColumnTypeLength()
                + " | ".length()
                + maxForeignColumnLength()
                + " |".length();
    }

    private int maxColumnNameLength() {
        return tables
                .stream()
                .flatMap(tableInfo -> tableInfo.columns.stream())
                .map(columnInfo -> columnInfo.columnName.length())
                .max((l1, l2) -> l1.equals(l2) ? 0 : l1 < l2 ? -1 : 1)
                .get();
    }

    private int maxColumnTypeLength() {
        return tables.
                stream()
                .flatMap(tableInfo -> tableInfo.columns.stream())
                .map(columnInfo -> columnInfo.type.length())
                .max((l1, l2) -> l1.equals(l2) ? 0 : l1 < l2 ? -1 : 1)
                .get();
    }

    private int maxForeignColumnLength() {
        return tables
                .stream()
                .flatMap(tableInfo -> tableInfo.foreignKeyColumns.stream())
                .map(columnInfo -> {
                    return columnInfo.referencedTableName().length() + ".".length()
                    + columnInfo.referencedTableName().length();
                })
                .max((l1, l2) -> l1.equals(l2) ? 0 : l1 < l2 ? -1 : 1)
                .get();
    }
}
