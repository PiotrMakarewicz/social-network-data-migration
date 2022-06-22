package mapping.loader.interactive;

import mapping.CSVSchemaMapping;
import mapping.SchemaMapping;
import mapping.edge.CSVEdgeMapping;
import mapping.node.CSVNodeMapping;

import java.io.*;
import java.util.*;

import static java.lang.Integer.parseInt;
import static utils.CSVUtils.*;

public class InteractiveCSVMappingLoader {
    private CSVNodeMapping fromNodeMapping;
    private CSVNodeMapping toNodeMapping;
    private String edgeLabel;
    private Map<Integer, String> edgeMappedColumns;
    private final BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
    private final String csvInputPath;
    private final boolean withHeaders;
    private final char fieldTerminator = '\t';
    private List<String> headers;
    private int columnCnt;


    public InteractiveCSVMappingLoader(String csvInputPath, boolean withHeaders) {
        this.csvInputPath = csvInputPath;
        this.withHeaders = withHeaders;
        if (csvInputPath != null) {
            if (withHeaders) {
                this.headers = getHeaders(csvInputPath, fieldTerminator);
                this.columnCnt = 0;
            } else {
                this.headers = null;
                this.columnCnt = getColumnCnt(csvInputPath, fieldTerminator);
            }
        }
    }

    public SchemaMapping load(String filename) throws IOException {
        showHelp();
        do {
            System.out.print("\n=> ");
            String command = in.readLine();
            if (command == null) {
                System.out.println("Incorrect command");
                showHelp();
            } else if (command.matches("help")) {
                showHelp();
            } else if (command.matches("set from node mapping")) {
                setFromNodeMapping();
            } else if (command.matches("set to node mapping")) {
                setToNodeMapping();
            } else if (command.matches("set edge mapping")) {
                setEdgeMapping();
            } else if (command.matches("show mappings")) {
                showMappings();
            } else if (command.matches("head")) {
                head();
            } else if (command.matches("start migration")) {
                if (!validateMapping()) continue;
                if (questionYesNo("Are you sure?")) break;
            } else {
                System.out.println("Incorrect command");
                showHelp();
            }
        } while (true);

        var edgeMapping = new CSVEdgeMapping(edgeLabel, edgeMappedColumns, fromNodeMapping, toNodeMapping);
        var schemaMapping = new CSVSchemaMapping();
        schemaMapping.addEdgeMapping(edgeMapping);
        schemaMapping.addNodeMapping(fromNodeMapping);
        schemaMapping.addNodeMapping(toNodeMapping);
        return schemaMapping;
    }

    private boolean validateMapping() {
        if (edgeMappedColumns == null || edgeLabel == null) {
            System.out.println("Edge mapping not defined yet.");
            return false;
        }
        if (fromNodeMapping == null) {
            System.out.println("Source node mapping not defined yet.");
            return false;
        }
        if (toNodeMapping == null) {
            System.out.println("Destination node mapping not defined yet.");
            return false;
        }
        return true;
    }

    private void setEdgeMapping() throws IOException {
        String label;
        do {
            System.out.print("Edge label: ");
            label = in.readLine();
            if (label == null) {
                if (questionYesNo("Stop mapping creation?")) return;
            } else {
                break;
            }
        } while (true);

        Map<Integer, String> columnMappings = new HashMap<>();

        if (questionYesNo("Add column mappings?")) {
            columnMappings = createColumnMappings();
        }

        if (questionYesNo("Save edge mapping?")) {
            this.edgeLabel = label;
            this.edgeMappedColumns = columnMappings;
        }
    }

    private void showMappings() {
        System.out.println("Source node mapping: ");
        System.out.println(fromNodeMapping);

        System.out.println("Destination node mapping: ");
        System.out.println(toNodeMapping);

        System.out.println("Edge mapping: ");
        if (edgeLabel == null){
            System.out.println("null");
        }
        else System.out.printf("Label: %s", edgeLabel);

        if (edgeMappedColumns != null && ! edgeMappedColumns.isEmpty()) {
            System.out.println("\nEdge mapped columns: ");
            StringBuilder builder = new StringBuilder();
            for (Map.Entry<Integer, String> mapping : edgeMappedColumns.entrySet()) {
                builder.append("\t%d -> %s\n".formatted(mapping.getKey(), mapping.getValue()));
            }
            System.out.println(builder);
        }
    }


    private void showHelp() {
        String help = """
                List of available commands:
                help
                \tShows list of available commands.
                set from node mapping
                \tAllows to create or edit the source node mapping.
                set to node mapping
                \tAllows to create or edit the destination node mapping.
                set edge mapping
                \tAllows to create or edit the edge mapping.
                show mappings
                \tShows a detailed list of created mappings.
                head
                \tShows the first 10 rows of the dataset.
                start migration
                \tFinishes interactive mapping creation and proceeds to migrate data.

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
                System.out.println(csvInput.readLine());
            }
        } catch (IOException e) {
            System.out.printf("Error while reading file: %s%n", csvInputPath);
        }
    }

    private void setFromNodeMapping() throws IOException {
        var m = createNodeMapping();
        if (m != null)
            fromNodeMapping = m;
    }

    private void setToNodeMapping() throws IOException {
        var m = createNodeMapping();
        if (m != null)
            toNodeMapping = m;
    }

    private CSVNodeMapping createNodeMapping() throws IOException {
        String label;
        do {
            System.out.print("Node label: ");
            label = in.readLine();
            if (label.isBlank()) {
                if (questionYesNo("Stop mapping creation?"))
                    return null;
            } else {
                break;
            }
        } while (true);

        var mappedColumns = createColumnMappings();

        if (questionYesNo("Save node mapping?"))
            return new CSVNodeMapping(label, mappedColumns);
        else
            return null;
    }

    private Map<Integer, String> createColumnMappings() throws IOException {
        Map<String, String> columnMappings = new HashMap<>();


            boolean stopMappingCreation = false;
            do {
                boolean stopCurrentCreation = false;
                String column;
                if (withHeaders) {
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
                        if (!headers.contains(column)) {
                            System.out.println("Incorrect column name.");
                        } else {
                            break;
                        }
                    } while (true);
                } else {
                    do {
                        System.out.print("Column index: ");
                        column = in.readLine();
                        if (column.isBlank()) {
                            if (questionYesNo("Stop column mapping creation?")) {
                                stopMappingCreation = true;
                                break;
                            } else {
                                continue;
                            }
                        }
                        int index;
                        try {
                            index = parseInt(column);
                        } catch (NumberFormatException e) {
                            continue;
                        }
                        if (index >= columnCnt) {
                            System.out.printf("There are %d columns in the CSV file. Please provide a number between 0 and %d%n", columnCnt, columnCnt - 1);
                        } else {
                            break;
                        }

                    } while (true);
                }

                if (stopMappingCreation) break;
                String prop;
                do {
                    System.out.print("Property name: ");
                    prop = in.readLine();
                    if (prop.isBlank()) {
                        if (questionYesNo("Stop current mapping creation?")) {
                            if (questionYesNo("Stop column mapping creation?")) stopMappingCreation = true;
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

        if (withHeaders)
            return headersToIndexes(columnMappings, headers);
        else
            return keysToInt(columnMappings);
    }

    private boolean questionYesNo(String prompt) throws IOException {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            System.out.flush();
            String stop = in.readLine();
            if (stop == null) continue;
            if (stop.toLowerCase(Locale.ROOT).equals("y")) return true;
            if (stop.toLowerCase(Locale.ROOT).equals("n")) return false;
        }
    }
}
