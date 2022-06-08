package mapping.loader.json;

public class CSVMappingJsonSchema {
    private NodeJson fromNode;
    private NodeJson toNode;
    private EdgeJson edge;


    public CSVMappingJsonSchema(NodeJson fromNode, NodeJson toNode, EdgeJson edge) {
        this.fromNode = fromNode;
        this.toNode = toNode;
        this.edge = edge;
    }

    public NodeJson getFromNode() {
        return fromNode;
    }

    public NodeJson getToNode() {
        return toNode;
    }

    public EdgeJson getEdge() {
        return edge;
    }
}
