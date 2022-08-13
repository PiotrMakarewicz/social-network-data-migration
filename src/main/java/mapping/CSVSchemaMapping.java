package mapping;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import mapping.edge.CSVEdgeMapping;
import mapping.node.CSVNodeMapping;

public class CSVSchemaMapping {
    @Getter @Setter @SerializedName("fromNode") private CSVNodeMapping fromNodeMapping;
    @Getter @Setter @SerializedName("toNode") private CSVNodeMapping toNodeMapping;
    @Getter @Setter @SerializedName("edge") private CSVEdgeMapping edgeMapping;

    public String toString() {
        return "Source node mapping:\n" +
                fromNodeMapping.toString() + "\n" +
                "\nDestination node mapping:\n" +
                toNodeMapping.toString() + "\n" +
                "\nEdge mapping:\n" +
                edgeMapping.toString() + "\n";
    }
}
