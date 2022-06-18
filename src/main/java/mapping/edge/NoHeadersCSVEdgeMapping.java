package mapping.edge;

import mapping.node.CSVNodeMapping;
import mapping.node.NoHeadersCSVNodeMapping;
import mapping.node.NodeMapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NoHeadersCSVEdgeMapping extends EdgeMapping {
    private final Map<Integer, String> mappedColumns;

    private final NodeMapping fromNodeMapping;
    private final NodeMapping toNodeMapping;

    public NoHeadersCSVEdgeMapping(String edgeLabel, Map<Integer, String> mappedColumns, NoHeadersCSVNodeMapping fromNodeMapping, NoHeadersCSVNodeMapping toNodeMapping) {
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
}
