package mapping.edge;

import mapping.node.CSVNodeMapping;
import mapping.node.NodeMapping;
import org.w3c.dom.Node;

import java.util.Map;

public class CSVEdgeMapping extends EdgeMapping{
    private final Map<String, String> mappedColumns;

    private final NodeMapping fromNodeMapping;
    private final NodeMapping toNodeMapping;

    public CSVEdgeMapping(String edgeLabel, Map<String, String> mappedColumns, CSVNodeMapping fromNodeMapping, CSVNodeMapping toNodeMapping) {
        super(edgeLabel, fromNodeMapping.getNodeLabel(), toNodeMapping.getNodeLabel());
        this.fromNodeMapping = fromNodeMapping;
        this.toNodeMapping = toNodeMapping;
        this.mappedColumns = mappedColumns;
    }

    public Map<String, String> getMappedColumns() {
        return mappedColumns;
    }

    public NodeMapping getToNodeMapping() {
        return toNodeMapping;
    }

    public NodeMapping getFromNodeMapping() {
        return fromNodeMapping;
    }
}
