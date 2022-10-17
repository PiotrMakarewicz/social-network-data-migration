package pl.edu.agh.socialnetworkdatamigration.core.mapping.node;

import java.util.Map;
import java.util.Objects;

public class CSVNodeMapping extends NodeMapping {
    private final Map<Integer, String> mappedColumns;

    public CSVNodeMapping(String nodeLabel, Map<Integer, String> mappedColumns) {
        super(nodeLabel);
        this.mappedColumns = mappedColumns;
    }

    public Map<Integer, String> getMappedColumns() {
        return mappedColumns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CSVNodeMapping)) return false;
        if (!super.equals(o)) return false;
        return Objects.equals(mappedColumns, ((CSVNodeMapping) o).mappedColumns);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), mappedColumns);
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
