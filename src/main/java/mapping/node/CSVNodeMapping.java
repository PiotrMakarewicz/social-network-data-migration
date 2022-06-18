package mapping.node;

import java.util.Map;
import java.util.Objects;

public class CSVNodeMapping extends NodeMapping {
    private final Map<String, String> mappedColumns;

    public CSVNodeMapping(String nodeLabel, Map<String, String> mappedColumns) {
        super(nodeLabel);
        this.mappedColumns = mappedColumns;
    }

    public Map<String, String> getMappedColumns() {
        return mappedColumns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CSVNodeMapping that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(mappedColumns, that.mappedColumns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mappedColumns);
    }
}
