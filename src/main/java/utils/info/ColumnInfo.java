package utils.info;

import java.util.Objects;

public class ColumnInfo {
    public String columnName;
    public String tableName;
    public String type;

    public ColumnInfo(String columnName, String tableName, String type) {
        this.columnName = columnName;
        this.tableName = tableName;
        this.type = type;
    }

    @Override
    public String toString() {
        return "ColumnInfo{" +
                "columnName='" + columnName + '\'' +
                ", tableName='" + tableName + '\'' +
                ", type='" + type + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ColumnInfo that = (ColumnInfo) o;
        return columnName.equals(that.columnName) && tableName.equals(that.tableName) && type.equals(that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columnName, tableName, type);
    }
}