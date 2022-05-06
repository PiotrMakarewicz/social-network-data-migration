package mapping.node;

import java.util.Map;

public class SQLNodeMapping extends NodeMapping {
    private final String sqlTableName;
    private final Map<String, String> mappedColumns;

    public SQLNodeMapping(String nodeLabel, String sqlTableName, Map<String, String> mappedColumns) {
        super(nodeLabel);
        this.sqlTableName = sqlTableName;
        this.mappedColumns = mappedColumns;
    }

    public String getSqlTableName() {
        return sqlTableName;
    }

    public Map<String, String> getMappedColumns() {
        return mappedColumns;
    }
}
