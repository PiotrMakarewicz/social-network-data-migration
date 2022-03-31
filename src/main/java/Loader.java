import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Session;
import utils.SchemaLoader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Loader implements AutoCloseable {
    private final Driver driver;
    private final String postgresHost;
    private final String postgresDB;
    private final String postgresUser;
    private final String postgresPassword;

    public Loader(String neo4jHost, String neo4jUser, String neo4jPassword,
                  String postgresHost, String postgresDB, String postgresUser, String postgresPassword) {
        driver = GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword));
        this.postgresHost = postgresHost;
        this.postgresDB = postgresDB;
        this.postgresUser = postgresUser;
        this.postgresPassword = postgresPassword;
    }

    @Override
    public void close() {
        driver.close();
    }

    public void loadData() {
        try (Session session = driver.session()) {
            BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("Enter name of table from PostgreSQL database:");
                String tableName = stdin.readLine();
                if (tableName == null)
                    break;
                System.out.println("Enter name of node to which data will be loaded:");
                String nodeName = stdin.readLine();
                if (nodeName == null)
                    break;
                String call = buildApocCall(tableName, nodeName);
                System.out.println(call);
                session.writeTransaction(tx -> {
                    tx.run(call);
                    return null;
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
    private String buildApocCall(String tableName, String nodeName) {// String.format
        StringBuilder sb = new StringBuilder("CALL apoc.load.jdbc(\"jdbc:postgresql://");
        sb.append(postgresHost + "/" + postgresDB + "?user=" + postgresUser).
                append("&password=" + postgresPassword +"\"," + "\"" + tableName +
                        "\") YIELD row WITH row LIMIT 200 ").
                append("MERGE (n:" + nodeName + "{");
        try (SchemaLoader loader = new SchemaLoader(postgresHost, postgresDB, postgresUser, postgresPassword)){
            for (String column : loader.getTableColumnNames(tableName)) {
                sb.append(column + ":coalesce(row." + column + ", 'NULL'),");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        sb.setLength(sb.length() - 1);
        sb.append("});");
        return sb.toString();
    }

    public static void main(String[] args) {
        if (args.length != 7) {
            System.out.println("Usage: java Loader <Neo4j-hostname> <Neo4j-user> <Neo4j-password>" +
                    "<PostgreSQL-hostname> <PostgreSQL-database> <PostgreSQL-user> <PostgreSQL-password>");
            return;
        }
        try (Loader loader = new Loader(args[0], args[1], args[2], args[3], args[4], args[5], args[6])){
            loader.loadData();
        }
    }
}
