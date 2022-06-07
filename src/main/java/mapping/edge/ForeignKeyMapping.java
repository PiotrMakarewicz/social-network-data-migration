package mapping.edge;

public class ForeignKeyMapping extends SQLEdgeMapping {
    private final String foreignKeyTable;

    public ForeignKeyMapping(String edgeLabel, String fromNode, String toNode, String fromTable, String toTable, String foreignKeyTable) {
        super(edgeLabel, fromNode, toNode, fromTable, toTable);
        this.foreignKeyTable = foreignKeyTable;
    }

    public String getForeignKeyTable() {
        return foreignKeyTable;
    }
}
