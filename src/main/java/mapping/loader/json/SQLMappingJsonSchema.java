package mapping.loader.json;

import java.util.List;

public class SQLMappingJsonSchema {
    private List<NodeJson> nodes;
    private List<EdgeJson> edges;

    public List<NodeJson> getNodes() {
        return nodes;
    }

    public List<EdgeJson> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return "SQLMappingJsonSchema{" + "\n" +
                "nodes=" + nodes + ",\n" +
                "edges=" + edges + "\n" +
                '}';
    }
}
