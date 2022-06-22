package mapping.edge;

import mapping.node.CSVNodeMapping;
import mapping.node.NodeMapping;
import org.w3c.dom.Node;

import java.util.Map;

public class CSVEdgeMapping extends EdgeMapping{
    private final Map<Integer, String> mappedColumns;

    private final NodeMapping fromNodeMapping;
    private final NodeMapping toNodeMapping;

    public CSVEdgeMapping(String edgeLabel, Map<Integer, String> mappedColumns, CSVNodeMapping fromNodeMapping, CSVNodeMapping toNodeMapping) {
        super(edgeLabel, fromNodeMapping.getNodeLabel(), toNodeMapping.getNodeLabel());
        this.fromNodeMapping = fromNodeMapping;
        this.toNodeMapping = toNodeMapping;
        this.mappedColumns = mappedColumns;
    }

    public Map<Integer, String> getMappedColumns() {
        return mappedColumns;
    }

    public NodeMapping getToNodeMapping() {
        return toNodeMapping;
    }

    public NodeMapping getFromNodeMapping() {
        return fromNodeMapping;
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
