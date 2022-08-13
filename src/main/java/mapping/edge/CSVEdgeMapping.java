package mapping.edge;

import lombok.Getter;
import mapping.node.CSVNodeMapping;

import java.util.Map;

public class CSVEdgeMapping extends EdgeMapping{
    @Getter private final Map<Integer, String> mappedColumns;

    public CSVEdgeMapping(String edgeLabel, Map<Integer, String> mappedColumns, CSVNodeMapping fromNodeMapping, CSVNodeMapping toNodeMapping) {
        super(edgeLabel, fromNodeMapping.getNodeLabel(), toNodeMapping.getNodeLabel());
        this.mappedColumns = mappedColumns;
    }

    @Override
    public String toString() {
        String header =  super.toString() +
                """
                Mapped columns:
                """;
        StringBuilder builder = new StringBuilder(header);
        for (Map.Entry<Integer, String> mapping : mappedColumns.entrySet()) {
            builder.append("\t%d -> %s\n".formatted(mapping.getKey(), mapping.getValue()));
        }
        return builder.toString();
    }
}
