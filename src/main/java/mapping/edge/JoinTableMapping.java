package mapping.edge;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class JoinTableMapping extends SQLEdgeMapping{
    @Getter
    private final String joinTable;

    private Map<String, String> mappedColumns;

    public JoinTableMapping(String edgeLabel, String fromNode, String toNode, String fromTable, String toTable, String joinTable, Map<String, String> mappedColumns) {
        super(edgeLabel, fromNode, toNode, fromTable, toTable);
        this.joinTable = joinTable;
        this.mappedColumns = mappedColumns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JoinTableMapping that = (JoinTableMapping) o;
        return getJoinTable().equals(that.getJoinTable()) && getMappedColumns().equals(that.getMappedColumns());
    }

    public Map<String, String> getMappedColumns() {
        if (mappedColumns == null)
            mappedColumns = new HashMap<>();
        return mappedColumns;
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getJoinTable(), getMappedColumns());
    }

    @Override
    public String toString() {
        String header =  super.toString() +
                String.format(
                "Join table:        %s\n" +
                "Mapped columns:\n",
                        joinTable);
        StringBuilder builder = new StringBuilder(header);
        for (Map.Entry<String, String> mapping : mappedColumns.entrySet()) {
            builder.append(String.format("\t%s -> %s\n", mapping.getKey(), mapping.getValue()));
        }
        return builder.toString();
    }
}
