package mapping.node;

import java.util.Map;
import java.util.Objects;

public class NoHeadersCSVNodeMapping extends NodeMapping{
    private final Map<Integer, String> mappedColumns;

    public NoHeadersCSVNodeMapping(String nodeLabel, Map<Integer, String> mappedColumns) {
        super(nodeLabel);
        this.mappedColumns = mappedColumns;
    }

    public Map<Integer, String> getMappedColumns() {
        return mappedColumns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NoHeadersCSVNodeMapping that)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(mappedColumns, that.mappedColumns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mappedColumns);
    }
}
