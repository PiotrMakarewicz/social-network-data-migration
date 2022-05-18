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
        public String referencedColumnName;
        public String referencedTableName;
        public ColumnInfo(String columnName,
                          String tableName,
                          String referencedColumnName,
                          String referencedTableName) {
            this.columnName = columnName;
            this.tableName = tableName;
            this.referencedColumnName = referencedColumnName;
            this.referencedTableName = referencedTableName;
        }

        public static Optional<String> getForeignKeyReferencingTable(Collection<ColumnInfo> columnData,String referencedTableName) {
            return columnData
                    .stream()
                    .filter(columnInfo -> columnInfo.referencedTableName.equals(referencedTableName))
                    .map(columnInfo -> columnInfo.columnName)
                    .findFirst();
        }

        @Override
        public String toString() {
            return "ColumnInfo{" +
                    "columnName='" + columnName + '\'' +
                    ", tableName='" + tableName + '\'' +
                    ", referencedColumnName='" + referencedColumnName + '\'' +
                    ", referencedTableName='" + referencedTableName + '\'' +
                    '}';
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
     * Returns name of column in <code>tableName</code> referencing primary
     * key of <code>referencedTableName</code>. Current implementation assumes primary key
     * of <code>referencedTableName</code> is a single column.
     *
     * @param  tableName  name of table with foreign key column
     * @param  referencedTableName name of table referenced by foreign key column in <code>tableName</code>
     * @return      name of foreign key column
     */
    public String getForeignKeyColumnName(String tableName, String referencedTableName) {
        Collection<ColumnInfo> foreignKeys = this.getForeignKeys(tableName);
        return ColumnInfo.getForeignKeyReferencingTable(foreignKeys, referencedTableName)
                .orElseThrow(() -> new RuntimeException(
                        "Table %s has no foreign key column referencing table %s"
                                .formatted(tableName, referencedTableName)
                ));
    }

    public String getPrimaryKeyColumn(String tableName) {
        String columnName = null;
        String sql = """
                SELECT kcu.column_name FROM information_schema.key_column_usage kcu
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
            rs.next();
            columnName = rs.getObject(1, String.class);
            rs.close();
        } catch (SQLException e) {
            throw new RuntimeException("Couldn't get primary key column for table " + tableName + ": " + e.getMessage());
        }
        return columnName;
    }

    public List<ColumnInfo> getForeignKeys(String tableName) {
        List<ColumnInfo> result = new ArrayList<>();
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
                ColumnInfo columnInfo = new ColumnInfo(
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
