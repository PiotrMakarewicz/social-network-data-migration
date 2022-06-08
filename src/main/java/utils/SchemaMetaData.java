package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;

public class SchemaMetaData implements AutoCloseable {
    private final Connection connection;

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

    public SchemaMetaData(String postgresHost, String postgresDB, String postgresUser, String postgresPassword) {
        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://" + postgresHost + "/" +
                    postgresDB, postgresUser, postgresPassword);
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't establish connection with database");
        }
    }

    public SchemaMetaData(String configPath) throws IOException {
        Properties properties = new Properties();
        properties.load(new FileInputStream(configPath));
        String postgresHost = properties.getProperty("postgresHost");
        String postgresDB = properties.getProperty("postgresDB");
        String postgresUser = properties.getProperty("postgresUser");
        String postgresPassword = properties.getProperty("postgresPassword");
        try {
            this.connection = DriverManager.getConnection("jdbc:postgresql://" + postgresHost + "/" +
                    postgresDB, postgresUser, postgresPassword);
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't establish connection with database:" + e.getMessage());
        }
    }

    /**
     * Returns list of ForeignKeyInfo containing information about columns
     * in <code>tableName</code> referencing primary
     * keys of <code>referencedTableName</code>.
     *
     * @param  tableName  name of table with foreign key column
     * @param  referencedTableName name of table referenced by foreign key column in <code>tableName</code>
     * @return Returns list of ForeignKeyInfo containing information about columns
     *       in <code>tableName</code>
     */
    public List<ForeignKeyInfo> getForeignKeyInfoForTable(String tableName, String referencedTableName) {
        List<ForeignKeyInfo> foreignKeys = this.getForeignKeyInfo(tableName);
        return foreignKeys
                .stream()
                .filter(fkColumnInfo -> fkColumnInfo.referencedTableName().equals(referencedTableName))
                .toList();
    }

    public List<ColumnInfo> getPrimaryKeyColumns(String tableName) {
        List<ColumnInfo> primaryKeyColumns = new ArrayList<>();
        String sql = """
                SELECT kcu.column_name, kcu.table_name, c.data_type FROM information_schema.key_column_usage kcu
                INNER JOIN information_schema.table_constraints tc
                ON kcu.constraint_name = tc.constraint_name
                AND kcu.table_name = tc.table_name
                AND kcu.table_schema = tc.table_schema
                INNER JOIN information_schema.columns c
                ON kcu.column_name = c.column_name
                AND kcu.table_name = c.table_name
                WHERE kcu.table_schema = 'public' AND kcu.table_name = ?
                AND tc.constraint_type = 'PRIMARY KEY';
                """;
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, tableName);
            ResultSet rs = ps.executeQuery();
            while(rs.next()) {
                primaryKeyColumns.add(new ColumnInfo(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3)
                ));
            }
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get primary key column for table " + tableName + ": " + e.getMessage());
        }
        return primaryKeyColumns;
    }

    public List<ForeignKeyInfo> getForeignKeyInfo(String tableName) {
        List<ForeignKeyInfo> result = new ArrayList<>();
        String sql = """
                SELECT kcu_from.column_name, kcu_from.table_name, c_from.data_type, kcu_to.column_name, kcu_to.table_name, c_to.data_type
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
                AND kcu_to.constraint_schema = unique_constraint_schema
                INNER JOIN information_schema.columns c_from
                ON kcu_from.column_name = c_from.column_name
                AND kcu_from.table_name = c_from.table_name
                INNER JOIN information_schema.columns c_to
                ON kcu_to.column_name = c_to.column_name
                AND kcu_to.table_name = c_to.table_name;
                """;

        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setObject(1, tableName);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ColumnInfo foreignKeyColumn = new ColumnInfo(
                        rs.getString(1),
                        rs.getString(2),
                        rs.getString(3).toUpperCase()
                );
                ColumnInfo referencedColumn = new ColumnInfo(
                        rs.getString(4),
                        rs.getString(5),
                        rs.getString(6).toUpperCase()
                );
                ForeignKeyInfo columnInfo = new ForeignKeyInfo(
                        foreignKeyColumn,
                        referencedColumn);
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

    public List<ColumnInfo> getColumns(String tableName) {
        try {
            List<ColumnInfo> columns = new ArrayList<>();
            DatabaseMetaData metaData = connection.getMetaData();
            ResultSet rs = metaData.getColumns(connection.getCatalog(), connection.getSchema(), tableName, "%");
            while (rs.next()) {
                ColumnInfo columnInfo = new ColumnInfo(
                        rs.getString(4),
                        rs.getString(3),
                        JDBCType.valueOf(rs.getInt(5)).getName()
                );
                columns.add(columnInfo);
            }
            rs.close();
            return columns;
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get column info for table " + tableName);
        }
    }


    public List<TableInfo> getTables() {
        List<TableInfo> tables = new ArrayList<>();
        String sql = """
                SELECT table_name FROM information_schema.tables t
                WHERE t.table_schema = 'public'
                """;
        try (Statement statement = connection.createStatement()){
            ResultSet rs = statement.executeQuery(sql);
            while (rs.next()) {
                String tableName = rs.getString(1);
                List<ForeignKeyInfo> foreignKeyColumns = getForeignKeyInfo(tableName);
                List<ColumnInfo> columns = getColumns(tableName);
                tables.add(new TableInfo(tableName, columns, foreignKeyColumns));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get table info");
        }
        return tables;
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }
}
