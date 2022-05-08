package mapping.edge;

public class ForeignKeyMapping extends SQLEdgeMapping {
    private final String foreignKey;

    public ForeignKeyMapping(String edgeLabel, String fromNode, String toNode, String fromTable, String toTable, String foreignKey) {
        super(edgeLabel, fromNode, toNode, fromTable, toTable);
        this.foreignKey = foreignKey;
    }

    public String getForeignKey() {
        return foreignKey;
    }
}
