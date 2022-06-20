package mapping.loader.json;

import java.util.List;

public class XMLMappingJsonSchema {
    private List<XMLNodeJson> nodes;
    private List<XMLEdgeJson> edges;

    public List<XMLNodeJson> getNodes() {
        return nodes;
    }

    public List<XMLEdgeJson> getEdges() {
        return edges;
    }

    @Override
    public String toString() {
        return "XMLMappingJsonSchema{" + "\n" +
                "nodes=" + nodes + ",\n" +
                "edges=" + edges + "\n" +
                '}';
    }
}
