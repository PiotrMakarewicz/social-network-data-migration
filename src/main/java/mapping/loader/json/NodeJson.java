package mapping.loader.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class NodeJson {
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
        return Optional.ofNullable(mappedColumns).orElse(new HashMap<>());
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
