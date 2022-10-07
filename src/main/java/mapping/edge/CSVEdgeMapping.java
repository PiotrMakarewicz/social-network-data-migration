package mapping.edge;

import mapping.node.CSVNodeMapping;

import java.util.HashMap;
import java.util.Map;

public class CSVEdgeMapping extends EdgeMapping{

    private Map<Integer, String> mappedColumns;

    public CSVEdgeMapping(String edgeLabel, Map<Integer, String> mappedColumns, CSVNodeMapping fromNodeMapping, CSVNodeMapping toNodeMapping) {
        super(edgeLabel, fromNodeMapping.getNodeLabel(), toNodeMapping.getNodeLabel());
        this.mappedColumns = mappedColumns;
    }

    public Map<Integer, String> getMappedColumns() {
        if (mappedColumns == null)
            mappedColumns = new HashMap<>();
        return mappedColumns;
    }

    @Override
    public String toString() {
        String header =  super.toString() + "Mapped columns:\n";
        StringBuilder builder = new StringBuilder(header);
        for (Map.Entry<Integer, String> mapping : mappedColumns.entrySet()) {
            builder.append(String.format("\t%d -> %s\n", mapping.getKey(), mapping.getValue()));
        }
        return builder.toString();
    }
}
