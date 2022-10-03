package mapping.node;

import lombok.Getter;

import java.util.Map;
import java.util.Objects;

public class SQLNodeMapping extends NodeMapping {
    @Getter private final String sqlTableName;
    @Getter private final Map<String, String> mappedColumns;

    public SQLNodeMapping(String nodeLabel, String sqlTableName, Map<String, String> mappedColumns) {
        super(nodeLabel);
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
        String header =  super.toString() +
                """
                Table name:        %s
                Mapped columns:
                """.formatted(sqlTableName);
        StringBuilder builder = new StringBuilder(header);
        for (Map.Entry<String, String> mapping : mappedColumns.entrySet()) {
            builder.append("\t%s -> %s\n".formatted(mapping.getKey(), mapping.getValue()));
        }
        return builder.toString();
    }
}
