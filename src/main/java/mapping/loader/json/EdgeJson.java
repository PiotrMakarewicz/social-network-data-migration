package mapping.loader.json;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EdgeJson {
    private String edgeLabel;
    private String foreignKeyTable;
    private String from;
    private String to;
    private String joinTable;
    private Map<String, String> mappedColumns;

    public String getEdgeLabel() {
        return edgeLabel;
    }

    public String getForeignKeyTable() {
        return foreignKeyTable;
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
        return Optional.ofNullable(mappedColumns).orElse(new HashMap<>());
    }

    @Override
    public String toString() {
        return "Edge{" + "\n" +
                "edgeLabel='" + edgeLabel + '\'' + ",\n" +
                "foreignKeyTable='" + foreignKeyTable + '\'' + ",\n" +
                "from='" + from + '\'' + ",\n" +
                "to='" + to + '\'' + ",\n" +
                "joinTable='" + joinTable + '\'' + ",\n" +
                "mappedColumns=" + mappedColumns + "\n" +
                '}';
    }
}
