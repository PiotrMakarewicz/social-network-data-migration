package pl.edu.agh.socialnetworkdatamigration.core.mapping.node;

import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class SQLNodeMapping extends NodeMapping {
    @Getter
    private final String sqlTableName;
    @Getter
    private final Map<String, String> mappedColumns;

    public SQLNodeMapping(String nodeLabel, String sqlTableName, Map<String, String> mappedColumns, List<String> identifyingFields) {
        super(nodeLabel, identifyingFields);
        this.sqlTableName = sqlTableName;
        this.mappedColumns = mappedColumns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        SQLNodeMapping that = (SQLNodeMapping) o;
        return getSqlTableName().equals(that.getSqlTableName()) && getMappedColumns().equals(that.getMappedColumns());
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), getSqlTableName(), getMappedColumns());
    }

    @Override
    public String toString() {
        String header = super.toString() +
                String.format("Table name:        %s\n", sqlTableName) +
                "Mapped columns:\n";
        StringBuilder builder = new StringBuilder(header);
        for (Map.Entry<String, String> mapping : mappedColumns.entrySet()) {
            builder.append(String.format("\t%s -> %s\n", mapping.getKey(), mapping.getValue()));
        }
        return builder.toString();
    }

    public Optional<String> getColumnForField(String field) {
        return mappedColumns.entrySet()
                .stream()
                .filter(entry -> entry.getValue().equals(field))
                .findFirst()
                .map(Map.Entry::getKey);
    }
}
