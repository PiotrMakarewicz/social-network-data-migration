package mapping.edge;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import java.util.Objects;

public abstract class SQLEdgeMapping extends EdgeMapping {
    @Getter @SerializedName("from")
    private final String fromTable;

    @Getter @SerializedName("to")
    private final String toTable;

    protected SQLEdgeMapping(String edgeLabel, String fromNode, String toNode, String fromTable, String toTable) {
        super(edgeLabel, fromNode, toNode);
        this.fromTable = fromTable;
        this.toTable = toTable;
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
        return super.toString() + String.format(
                "Source table:      %s\n" +
                "Destination table: %s\n",
                fromTable, toTable);
    }
}
