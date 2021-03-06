package mapping.edge;

import java.util.Map;
import java.util.Objects;

public class JoinTableMapping extends SQLEdgeMapping{
    private final String joinTable;
    private final Map<String, String> mappedColumns;

    public JoinTableMapping(String edgeLabel, String fromNode, String toNode, String fromTable, String toTable, String joinTable, Map<String, String> mappedColumns) {
        super(edgeLabel, fromNode, toNode, fromTable, toTable);
        this.joinTable = joinTable;
        this.mappedColumns = mappedColumns;
    }

    public String getJoinTable() {
        return joinTable;
    }

    public Map<String, String> getMappedColumns() {
        return mappedColumns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        JoinTableMapping that = (JoinTableMapping) o;
        return getJoinTable().equals(that.getJoinTable()) && getMappedColumns().equals(that.getMappedColumns());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getJoinTable(), getMappedColumns());
    }
    @Override
    public String toString() {
        String header =  super.toString() +
                """
                Join table:        %s
                Mapped columns:
                """.formatted(joinTable);
        StringBuilder builder = new StringBuilder(header);
        for (Map.Entry<String, String> mapping : mappedColumns.entrySet()) {
            builder.append("\t%s -> %s\n".formatted(mapping.getKey(), mapping.getValue()));
        }
        return builder.toString();
    }
}
