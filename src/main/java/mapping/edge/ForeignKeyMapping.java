package mapping.edge;

import java.util.Objects;

public class ForeignKeyMapping extends SQLEdgeMapping {
    private final String foreignKeyTable;

    public ForeignKeyMapping(String edgeLabel, String fromNode, String toNode, String fromTable, String toTable, String foreignKeyTable) {
        super(edgeLabel, fromNode, toNode, fromTable, toTable);
        this.foreignKeyTable = foreignKeyTable;
    }

    public String getForeignKeyTable() {
        return foreignKeyTable;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        ForeignKeyMapping mapping = (ForeignKeyMapping) o;
        return getForeignKeyTable().equals(mapping.getForeignKeyTable());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getForeignKeyTable());
    }

    @Override
    public String toString() {
        return super.toString() +
                """
                Foreign key table: %s
                """.formatted(foreignKeyTable);
    }
}
