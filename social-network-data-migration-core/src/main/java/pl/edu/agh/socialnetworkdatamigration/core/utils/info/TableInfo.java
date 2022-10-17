package pl.edu.agh.socialnetworkdatamigration.core.utils.info;

import java.util.List;
import java.util.Optional;

public class TableInfo {
    public List<ColumnInfo> columns;
    public List<ForeignKeyInfo> foreignKeyColumns;
    public String tableName;

    public TableInfo(String tableName, List<ColumnInfo> columns, List<ForeignKeyInfo> foreignKeyColumns) {
        this.tableName = tableName;
        this.columns = columns;
        this.foreignKeyColumns = foreignKeyColumns;
    }

    public Optional<ForeignKeyInfo> getForeignKeyInfoForColumn(ColumnInfo column) {
        return foreignKeyColumns
                .stream()
                .filter(foreignKeyInfo -> foreignKeyInfo.foreignKeyColumn.equals(column))
                .findFirst();
    }
}