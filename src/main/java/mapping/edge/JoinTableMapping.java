package mapping.edge;

import java.util.Map;

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
}
