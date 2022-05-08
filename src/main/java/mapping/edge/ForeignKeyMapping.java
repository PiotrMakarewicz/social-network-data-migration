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

    public String getTableFromForeignKey() {
        return this.foreignKey.split("\\.")[0];
    }

    public String getColumnFromForeignKey() {
        return this.foreignKey.split("\\.")[1];
    }
}
