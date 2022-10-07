package utils.info;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DatabaseInfo {
    public List<TableInfo> tables;

    public DatabaseInfo(List<TableInfo> tables) {
        this.tables = tables;
    }

    @Override
    public String toString() {
        List<String> tableStrings = new ArrayList<>();
        tables.forEach(table -> tableStrings.add(getTableInfoStr(table)));
        return String.join("\n", tableStrings);
    }

    public String tableToString(String tableName) {
        Optional<TableInfo> tableInfo =
                tables
                        .stream()
                        .filter(t -> t.tableName.equals(tableName))
                        .findFirst();
        if (tableInfo.isEmpty())
            return "No such table";
        return getTableInfoStr(tableInfo.get());
    }

    public boolean hasTable(String tableName) {
        return tables
                .stream()
                .map(tableInfo -> tableInfo.tableName)
                .anyMatch(table ->table.equals(tableName));
    }

    public boolean hasForeignKey(String fromTable, String toTable) {
        return tables
                .stream()
                .filter(tableInfo -> tableInfo.tableName.equals(fromTable))
                .flatMap(tableInfo -> tableInfo.foreignKeyColumns.stream())
                .anyMatch(foreignKeyInfo -> foreignKeyInfo.referencedTableName().equals(toTable));
    }

    public boolean hasColumn(String tableName, String columnName) {
        return tables
                .stream()
                .filter(tableInfo -> tableInfo.tableName.equals(tableName))
                .flatMap(tableInfo -> tableInfo.columns.stream())
                .map(columnInfo -> columnInfo.columnName)
                .anyMatch(c -> c.equals(columnName));
    }

    private String getTableInfoStr(TableInfo table){
        StringBuilder stringBuilder = new StringBuilder();
        String tableName = table.tableName;
        int rowLen = maxRowLength();

        String roof = "-".repeat(rowLen);
        String namePaddingLeft = " ".repeat((rowLen - tableName.length() - "|".length() * 2) / 2);
        String namePaddingRight = namePaddingLeft;
        if ((rowLen - tableName.length()) % 2 == 1)
            namePaddingRight += " ";

        String header = String.format(
                "%s\n" +
                "|%s%s%s|\n" +
                "%s\n", roof, namePaddingLeft, tableName, namePaddingRight, roof);
        stringBuilder.append(header);

        for (ColumnInfo column : table.columns) {
            String columnName = column.columnName;
            String columnType = column.type;

            String columnNamePadding = " ".repeat((maxColumnNameLength() - columnName.length()));
            String columnTypePadding = " ".repeat((maxColumnTypeLength() - columnType.length()));

            String foreignColumnName = "";
            if (table.getForeignKeyInfoForColumn(column).isPresent()) {
                ForeignKeyInfo foreignKeyInfo = table.getForeignKeyInfoForColumn(column).get();
                foreignColumnName = foreignKeyInfo.referencedTableName() + "." + foreignKeyInfo.referencedColumnName();
            }
            String foreignColumnPadding = " ".repeat((maxForeignColumnLength() - foreignColumnName.length()));

            String row = String.format("| %s%s | %s%s | %s%s |",
                    column.columnName,
                    columnNamePadding,
                    column.type,
                    columnTypePadding,
                    foreignColumnName,
                    foreignColumnPadding
            );
            stringBuilder.append(row);
        }
        stringBuilder.append(roof);
        return stringBuilder.toString();
    }

    private int maxRowLength() {
        return "| ".length()
                + maxColumnNameLength()
                + " | ".length()
                + maxColumnTypeLength()
                + " | ".length()
                + maxForeignColumnLength()
                + " |".length();
    }

    private int maxColumnNameLength() {
        return tables
                .stream()
                .flatMap(tableInfo -> tableInfo.columns.stream())
                .map(columnInfo -> columnInfo.columnName.length())
                .max((l1, l2) -> l1.equals(l2) ? 0 : l1 < l2 ? -1 : 1)
                .get();
    }

    private int maxColumnTypeLength() {
        return tables.
                stream()
                .flatMap(tableInfo -> tableInfo.columns.stream())
                .map(columnInfo -> columnInfo.type.length())
                .max((l1, l2) -> l1.equals(l2) ? 0 : l1 < l2 ? -1 : 1)
                .get();
    }

    private int maxForeignColumnLength() {
        return tables
                .stream()
                .flatMap(tableInfo -> tableInfo.foreignKeyColumns.stream())
                .map(columnInfo -> {
                    return columnInfo.referencedTableName().length() + ".".length()
                            + columnInfo.referencedTableName().length();
                })
                .max((l1, l2) -> l1.equals(l2) ? 0 : l1 < l2 ? -1 : 1)
                .get();
    }
}
