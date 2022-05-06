package mapping.loader.json;

import java.util.Map;

public class Node {
    private String sqlTableName;
    private String nodeLabel;
    private Map<String, String> mappedColumns;

    public String getSqlTableName() {
        return sqlTableName;
    }

    public String getNodeLabel() {
        return nodeLabel;
    }

    public Map<String, String> getMappedColumns() {
        return mappedColumns;
    }

    @Override
    public String toString() {
        return "Node{" + "\n" +
                "sqlTableName='" + sqlTableName + '\'' + ",\n" +
                "nodeLabel='" + nodeLabel + '\'' + ",\n" +
                "mappedColumns=" + mappedColumns + "\n" +
                '}';
    }
}
