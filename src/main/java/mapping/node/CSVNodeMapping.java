package mapping.node;

import java.util.Map;

public class CSVNodeMapping extends NodeMapping {
    private final Map<String, String> mappedColumns;

    public CSVNodeMapping(String nodeLabel, Map<String, String> mappedColumns) {
        super(nodeLabel);
        this.mappedColumns = mappedColumns;
    }

    public Map<String, String> getMappedColumns() {
        return mappedColumns;
    }
}
