package mapping.loader.json;

import java.util.Map;

public class Edge {
    private String edgeLabel;
    private String foreignKey;
    private String from;
    private String to;
    private String joinTable;
    private Map<String, String> mappedColumns;

    public String getEdgeLabel() {
        return edgeLabel;
    }

    public String getForeignKey() {
        return foreignKey;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getJoinTable() {
        return joinTable;
    }

    public Map<String, String> getMappedColumns() {
        return mappedColumns;
    }

    @Override
    public String toString() {
        return "Edge{" + "\n" +
                "edgeLabel='" + edgeLabel + '\'' + ",\n" +
                "foreignKey='" + foreignKey + '\'' + ",\n" +
                "from='" + from + '\'' + ",\n" +
                "to='" + to + '\'' + ",\n" +
                "joinTable='" + joinTable + '\'' + ",\n" +
                "mappedColumns=" + mappedColumns + "\n" +
                '}';
    }
}
