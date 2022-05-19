package utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class SchemaMetaData implements AutoCloseable {
    private final Connection connection;

    public class ColumnInfo {
        public String columnName;
        public String tableName;

        public ColumnInfo(String columnName, String tableName) {
            this.columnName = columnName;
            this.tableName = tableName;
        }

        @Override
        public String toString() {
            return "ColumnInfo{" +
                    "columnName='" + columnName + '\'' +
                    ", tableName='" + tableName + '\'' +
                    '}';
        }
    }

    public class FKColumnInfo {
        public ColumnInfo foreignKeyColumn;
        public ColumnInfo referencedColumn;
        public FKColumnInfo(String columnName,
                          String tableName,
                          String referencedColumnName,
                          String referencedTableName) {
            this.foreignKeyColumn = new ColumnInfo(
                    columnName,
                    tableName
            );
            this.referencedColumn = new ColumnInfo(
                    referencedColumnName,
                    referencedTableName
            );
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
    }

    public SchemaMetaData(String postgresHost, String postgresDB, String postgresUser, String postgresPassword) {
        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://" + postgresHost + "/" +
                    postgresDB, postgresUser, postgresPassword);
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't establish connection with database");
        }

    }

    /**
     * Returns list of FKColumnInfo containing information about columns
     * in <code>tableName</code> referencing primary
     * keys of <code>referencedTableName</code>.
     *
     * @param  tableName  name of table with foreign key column
     * @param  referencedTableName name of table referenced by foreign key column in <code>tableName</code>
     * @return Returns list of FKColumnInfo containing information about columns
     *       in <code>tableName</code>
     */
    public List<FKColumnInfo> getForeignKeyColumnsForTable(String tableName, String referencedTableName) {
        List<FKColumnInfo> foreignKeys = this.getForeignKeyColumns(tableName);
        return foreignKeys
                .stream()
                .filter(fkColumnInfo -> fkColumnInfo.referencedTableName().equals(referencedTableName))
                .toList();
    }

    public List<ColumnInfo> getPrimaryKeyColumns(String tableName) {
        List<ColumnInfo> primaryKeyColumns = new ArrayList<>();
        String sql = """
                SELECT kcu.column_name, kcu.table_name FROM information_schema.key_column_usage kcu
                INNER JOIN information_schema.table_constraints tc
                ON kcu.constraint_name = tc.constraint_name
                AND kcu.table_name = tc.table_name
                AND kcu.table_schema = tc.table_schema
                WHERE kcu.table_schema = 'public' AND kcu.table_name = ?
                AND tc.constraint_type = 'PRIMARY KEY';
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, tableName);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                primaryKeyColumns.add(new ColumnInfo(
                        rs.getString(1),
                        rs.getString(2)
                ));
            }
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get primary key column for table " + tableName + ": " + e.getMessage());
        }
        return primaryKeyColumns;
    }

    public List<FKColumnInfo> getForeignKeyColumns(String tableName) {
        List<FKColumnInfo> result = new ArrayList<>();
        String sql = """
                SELECT kcu_from.column_name, kcu_from.table_name, kcu_to.column_name, kcu_to.table_name
                FROM information_schema.table_constraints tc
                INNER JOIN information_schema.key_column_usage kcu_from
                ON tc.table_schema = kcu_from.table_schema
                AND tc.table_name = kcu_from.table_name
                AND tc.constraint_name = kcu_from.constraint_name
                AND tc.constraint_type = 'FOREIGN KEY'
                AND tc.table_name = ?
                INNER JOIN information_schema.referential_constraints rc
                ON kcu_from.constraint_name = rc.constraint_name
                AND kcu_from.constraint_schema = rc.constraint_schema
                INNER JOIN information_schema.key_column_usage kcu_to
                ON kcu_to.constraint_name = rc.unique_constraint_name
                AND kcu_to.constraint_schema = unique_constraint_schema;
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, tableName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                FKColumnInfo columnInfo = new FKColumnInfo(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3),
                        rs.getString(4));
                result.add(columnInfo);
            }
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get foreign keys from table " + tableName + ": " + e.getMessage());
        }
        return result;
    }

    public List<String> getColumnNames(String tableName) {
        String sql = """
                SELECT column_name FROM information_schema.columns  
                WHERE table_schema = 'public' AND table_name = ?
                ORDER BY ordinal_position
                """;
        List<String> columns = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, tableName);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                columns.add(rs.getString(1));
            }
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get column names for table " + tableName + ": " + e.getMessage());
        }
        return columns;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
