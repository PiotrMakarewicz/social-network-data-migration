package pl.edu.agh.socialnetworkdatamigration.core.mapping;

import com.google.gson.annotations.SerializedName;
import lombok.Getter;
import lombok.Setter;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.CSVEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.CSVNodeMapping;

public class CSVSchemaMapping extends SchemaMapping<CSVNodeMapping, CSVEdgeMapping> {
    @Getter @Setter @SerializedName("fromNode")
    private CSVNodeMapping fromNodeMapping;
    @Getter @Setter @SerializedName("toNode")
    private CSVNodeMapping toNodeMapping;
    @Getter @Setter @SerializedName("edge")
    private CSVEdgeMapping edgeMapping;

    public String toString() {
        return "Source node mapping:\n" +
                fromNodeMapping.toString() + "\n" +
                "\nDestination node mapping:\n" +
                toNodeMapping.toString() + "\n" +
                "\nEdge mapping:\n" +
                edgeMapping.toString() + "\n";
    }
}
