package mapping.edge;

import java.util.Objects;

public abstract class SQLEdgeMapping extends EdgeMapping {
    private final String fromTable;
    private final String toTable;

    protected SQLEdgeMapping(String edgeLabel, String fromNode, String toNode, String fromTable, String toTable) {
        super(edgeLabel, fromNode, toNode);
        this.fromTable = fromTable;
        this.toTable = toTable;
    }

    public String getFromTable() {
        return fromTable;
    }

    public String getToTable() {
        return toTable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SQLEdgeMapping that = (SQLEdgeMapping) o;
        return getFromTable().equals(that.getFromTable()) && getToTable().equals(that.getToTable());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getFromTable(), getToTable());
    }

    @Override
    public String toString() {
        return super.toString() +
                """
                Source table:      %s
                Destination table: %s
                """.formatted(fromTable, toTable);
    }
}
