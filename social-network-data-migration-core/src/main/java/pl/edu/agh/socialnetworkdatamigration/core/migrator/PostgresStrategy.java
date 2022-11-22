package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import pl.edu.agh.socialnetworkdatamigration.core.mapping.SQLSchemaMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.edge.SQLEdgeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.mapping.node.SQLNodeMapping;
import pl.edu.agh.socialnetworkdatamigration.core.utils.SchemaMetaData;

import java.util.*;

public abstract class PostgresStrategy extends MigrationStrategy<SQLSchemaMapping> {
    protected final SchemaMetaData schemaMetaData;

    protected PostgresStrategy(SchemaMetaData schemaMetaData) {
        this.schemaMetaData = schemaMetaData;
    }

    @Override
    public final List<Set<String>> createMigrationQueries(SQLSchemaMapping schemaMapping) {
        List<Set<String>> operations = new ArrayList<>();

        Set<String> indexCreationOps = new HashSet<>();
        operations.add(indexCreationOps);
        schemaMapping.getNodeMappings().forEach(n -> indexCreationOps.add(createIndexQuery(n)));

        Set<String> nodeCreationOps = new HashSet<>();
        operations.add(nodeCreationOps);
        schemaMapping.getNodeMappings().forEach(n -> nodeCreationOps.add(createNodeQuery(n)));

        independentMappings(schemaMapping)
                .forEach(set -> {
                    Set<String> edgeCreationOps = new HashSet<>();
                    operations.add(edgeCreationOps);
                    set.forEach(e -> edgeCreationOps.add(createEdgeQuery(schemaMapping, e)));
                });

        return operations;
    }

    protected abstract String createIndexQuery(SQLNodeMapping nodeMapping);

    protected abstract String createNodeQuery(SQLNodeMapping nodeMapping);

    protected abstract String createEdgeQuery(SQLSchemaMapping schemaMapping, SQLEdgeMapping edgeMapping);

    protected List<Set<SQLEdgeMapping>> independentMappings(SQLSchemaMapping schemaMapping) {
        Queue<SQLEdgeMapping> edgeMappings = new LinkedList<>(schemaMapping.getEdgeMappings());
        List<Set<SQLEdgeMapping>> independentMappings = new ArrayList<>();

        while (!edgeMappings.isEmpty()) {
            Set<SQLEdgeMapping> mappingSet = new HashSet<>();
            independentMappings.add(mappingSet);

            for (SQLEdgeMapping edgeMapping : edgeMappings) {
                if (canBeAddedTo(mappingSet, edgeMapping)) {
                    mappingSet.add(edgeMapping);
                }
            }

            edgeMappings.removeAll(mappingSet);
        }

        return independentMappings;
    }

    private boolean canBeAddedTo(Set<SQLEdgeMapping> independentMappings, SQLEdgeMapping edgeMapping) {
        return independentMappings.stream().allMatch(e -> areIndependent(edgeMapping, e));
    }

    private boolean areIndependent(SQLEdgeMapping e1, SQLEdgeMapping e2) {
        return Collections.disjoint(
                Set.of(e1.getFromTable(), e1.getToTable()),
                Set.of(e2.getFromTable(), e2.getToTable())
        );
    }
}
