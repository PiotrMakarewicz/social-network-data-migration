package mapping.loader.json;

import java.util.List;

public class JsonSchema {
    private List<Node> nodes;
    private List<Edge> edges;

    public List<Node> getNodes() {
        return nodes;
    }

    public List<Edge> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return "JsonSchema{" + "\n" +
                "nodes=" + nodes + ",\n" +
                "edges=" + edges + "\n" +
                '}';
    }
}
