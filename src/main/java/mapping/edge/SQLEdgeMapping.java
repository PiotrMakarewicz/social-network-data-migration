package mapping.edge;

public abstract class SQLEdgeMapping extends EdgeMapping {
    private final String fromTable;
    private final String toTable;

    protected SQLEdgeMapping(String edgeLabel, String fromTable, String toTable) {
        super(edgeLabel);
        this.fromTable = fromTable;
        this.toTable = toTable;
    }

    public String getFromTable() {
        return fromTable;
    }

    public String getToTable() {
        return toTable;
    }
}
