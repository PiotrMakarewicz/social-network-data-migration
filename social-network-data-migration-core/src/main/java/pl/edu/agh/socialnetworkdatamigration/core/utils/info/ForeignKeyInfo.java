package pl.edu.agh.socialnetworkdatamigration.core.utils.info;

import java.util.Objects;

public class ForeignKeyInfo {
    public ColumnInfo foreignKeyColumn;
    public ColumnInfo referencedColumn;
    public ForeignKeyInfo(ColumnInfo foreignKeyColumn,
                          ColumnInfo referencedColumn) {
        this.foreignKeyColumn = foreignKeyColumn;
        this.referencedColumn = referencedColumn;
    }

    @Override
    public String toString() {
        return "FKColumnInfo{" +
                "foreignKeyColumn=" + foreignKeyColumn +
                ", referencedColumn=" + referencedColumn +
                '}';
    }

    public String columnName() {
        return foreignKeyColumn.columnName;
    }

    public String tableName() {
        return foreignKeyColumn.tableName;
    }

    public String referencedColumnName() {
        return referencedColumn.columnName;
    }
    public String referencedTableName() {
        return referencedColumn.tableName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ForeignKeyInfo that = (ForeignKeyInfo) o;
        return foreignKeyColumn.equals(that.foreignKeyColumn) && referencedColumn.equals(that.referencedColumn);
    }

    @Override
    public int hashCode() {
        return Objects.hash(foreignKeyColumn, referencedColumn);
    }
}
