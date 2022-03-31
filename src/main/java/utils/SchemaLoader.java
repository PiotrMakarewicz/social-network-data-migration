package utils;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SchemaLoader implements AutoCloseable {
    private final Connection connection;

    public SchemaLoader(String postgresHost, String postgresDB, String postgresUser, String postgresPassword) throws SQLException {
        this.connection = DriverManager.getConnection("jdbc:postgresql://" + postgresHost + "/" +
                postgresDB, postgresUser, postgresPassword);
    }

    @Override
    public void close() throws Exception {
        connection.close();
    }

    public List<String> getTableColumnNames(String tableName) {
        String sql = "SELECT column_name FROM information_schema.columns " +
                "WHERE table_schema = 'public' AND table_name = '" + tableName + "' " +
                "ORDER BY ordinal_position;";
        List<String> columns = new ArrayList<>();
        try (PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet rs = statement.executeQuery()){
            while (rs.next()) {
                columns.add(rs.getString(1));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return columns;
    }
}
