package mapping.loader.interactive;

import mapping.CSVSchemaMapping;
import mapping.SchemaMapping;
import mapping.edge.CSVEdgeMapping;
import mapping.edge.EdgeMapping;
import mapping.edge.NoHeadersCSVEdgeMapping;
import mapping.edge.SQLEdgeMapping;
import mapping.node.CSVNodeMapping;
import mapping.node.NoHeadersCSVNodeMapping;
import mapping.node.NodeMapping;
import mapping.node.SQLNodeMapping;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;
import static java.util.List.of;

public class InteractiveCSVMappingLoader {
    private final Map<String, NodeMapping> nodeMappings = new HashMap<>();
    private final Map<String, EdgeMapping> edgeMappings = new HashMap<>();
    private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    private final String csvInputPath;
    private final CSVSchemaMapping schemaMapping = new CSVSchemaMapping();
    private final boolean withHeaders;
    private final char fieldTerminator = '\t';
    private final List<String> headers;
    private final int columnCnt;

    private int nodeCounter = 0;

    public InteractiveCSVMappingLoader(String csvInputPath, boolean withHeaders) {
        this.csvInputPath = csvInputPath;
        this.withHeaders = withHeaders;
        if (withHeaders) {
            this.headers = getHeaders();
            this.columnCnt = 0;
        }
        else {
            this.headers = null;
            this.columnCnt = getColumnCnt();
        }
    }

    private List<String> getHeaders() {
        return List.of(readFirstLine().split(Pattern.quote(String.valueOf(fieldTerminator))));
    }

    private int getColumnCnt() {
        return (int) (readFirstLine().chars().filter(c -> c == fieldTerminator).count() + 1);
    }

    private String readFirstLine() {
        try (BufferedReader file = new BufferedReader(new FileReader(csvInputPath))) {
            return file.readLine();
        } catch (IOException e) {
            throw new RuntimeException(String.format("Error while reading file: %s%n", csvInputPath), e);
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
            } else if (command.matches("add node mapping")) { // TODO add from node mapping, add to node mapping
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
            } else if (command.matches("head")) {
                head();
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

        do {
            System.out.println("Source node mapping:");
        }


        // TODO node mappings from and to

        // TODO column mappings
    }

    private NodeMapping getNodeMapping(String prompt){
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
    }

    private void listNodeMappings(){
        System.out.println();
        for (Map.Entry<String, NodeMapping> entry : nodeMappings.entrySet()) {
            System.out.printf("Mapping name:      %s%n", entry.getKey());
            System.out.println(withHeaders ? (CSVNodeMapping) entry.getValue() : (NoHeadersCSVNodeMapping) entry.getValue());
        }
    }
    private void listEdgeMappings(){
        System.out.println();
        for (Map.Entry<String, EdgeMapping> entry : edgeMappings.entrySet()) {
            System.out.printf("Mapping name:      %s%n", entry.getKey());
            System.out.println(withHeaders ? (CSVEdgeMapping) entry.getValue() : (NoHeadersCSVEdgeMapping) entry.getValue());
        }
    }

    private void deleteNodeMapping(String mappingName){
        NodeMapping nodeMapping = nodeMappings.remove(mappingName);
        schemaMapping.getNodeMappings().remove(nodeMapping);
    }
    private void deleteEdgeMapping(String mappingName){
        EdgeMapping edgeMapping = edgeMappings.remove(mappingName);
        schemaMapping.getEdgeMappings().remove(edgeMapping);
    }

    private void showHelp() {
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
                head
                \tShows first 10 rows of the dataset
                quit
                \tQuits interactive mapping creation and proceeds to migrate data.

                Use ENTER when asked for input to stop mapping creation.
                """;
        System.out.println(help);
    }

    private void head() {
        FileReader file;
        try {
            file = new FileReader(csvInputPath);
        } catch (FileNotFoundException e) {
            System.out.printf("Cannot read file: %s%n", csvInputPath);
            return;
        }

        try (BufferedReader csvInput = new BufferedReader(file)) {
            for (int i = 0; i < 10; i++) {
                System.out.print(csvInput.readLine());
            }
        } catch (IOException e) {
            System.out.printf("Error while reading file: %s%n", csvInputPath);
        }
    }

    private void addNodeMapping() throws IOException {
        String label;
        do {
            System.out.print("Node label: ");
            label = in.readLine();
            if (label.isBlank()) {
                if (questionYesNo("Stop mapping creation?"))
                    return;
            } else {
                break;
            }
        } while (true);

        String index;
        do {
            System.out.print("Column index: ");
            index = in.readLine();
            if (index.isBlank()) {
                if (questionYesNo("Stop mapping creation?"))
                    return;
            }
        } while (!index.matches("^\\d$"));

        NodeMapping nodeMapping;

        if (withHeaders){
            var mappedColumns = createColumnMappings();
            nodeMapping = new CSVNodeMapping(label, mappedColumns);
        }else {
            var mappedColumns = createNoHeaderColumnMappings();
            nodeMapping = new NoHeadersCSVNodeMapping(label, mappedColumns);
        }

        if (questionYesNo("Save mapping?") && !nodeMappings.containsValue(nodeMapping)) {
            String name = label + nodeCounter++;
            nodeMappings.put(name, nodeMapping);
            schemaMapping.addNodeMapping(nodeMapping);
        }
    }

    private Map<String, String> createColumnMappings() throws IOException {
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
                    if (! headers.contains(column)) {
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

    private Map<Integer, String> createNoHeaderColumnMappings() throws IOException {
        Map<Integer, String> columnMappings = new HashMap<>();
        if (questionYesNo("Add column mappings?")) {
            boolean stopMappingCreation = false;
            do {
                boolean stopCurrentCreation = false;
                int column = 0;
                do {
                    System.out.print("Column index: ");
                    String columnStr = in.readLine();
                    if (columnStr == null) {
                        if (questionYesNo("Stop column mapping creation?")) {
                            stopMappingCreation = true;
                            break;
                        } else {
                            continue;
                        }
                    }
                    column = parseInt(columnStr);
                    if (column >= columnCnt) {
                        System.out.printf("There are %d columns in the CSV file. Please provide a number between 0 and %d%n", columnCnt, columnCnt - 1);
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
