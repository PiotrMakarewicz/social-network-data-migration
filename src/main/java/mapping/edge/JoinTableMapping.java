package mapping.edge;

import java.util.Map;

public class JoinTableMapping extends SQLEdgeMapping{
    private String joinTable;
    private Map<String, String> mappedColumns;

    public JoinTableMapping(String edgeLabel, String fromTable, String toTable, String joinTable, Map<String, String> mappedColumns) {
        super(edgeLabel, fromTable, toTable);
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
