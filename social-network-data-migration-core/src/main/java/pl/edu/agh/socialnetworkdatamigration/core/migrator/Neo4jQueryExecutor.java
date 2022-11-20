package pl.edu.agh.socialnetworkdatamigration.core.migrator;

import org.neo4j.driver.*;

import java.util.List;

public class Neo4jQueryExecutor implements AutoCloseable {

    private final Driver neo4jDriver;

    public Neo4jQueryExecutor(Driver neo4jDriver){
        this.neo4jDriver = neo4jDriver;
    }

    public static Neo4jQueryExecutor createFrom(String neo4jHost, String neo4jUser, String neo4jPassword){
        return new Neo4jQueryExecutor(GraphDatabase.driver("neo4j://" + neo4jHost, AuthTokens.basic(neo4jUser, neo4jPassword)));
    }

    public void executeInOneTransaction(List<String> queries) {
        try (Session session = neo4jDriver.session()){
            session.writeTransaction(tx -> {
                for (String query: queries)
                    runQueryWithinTransaction(tx, query);
                return null;
            });
        }
    }

    public void execute(String query) {
        executeInOneTransaction(List.of(query));
    }

    private void runQueryWithinTransaction(Transaction tx, String query) {
        System.out.println(query);
        long start = System.currentTimeMillis();
        tx.run(query);
        long end = System.currentTimeMillis();
        System.out.printf("Time taken: %s s%n", (end - start) / 1000);
    }

    @Override
    public void close() throws Exception {
        neo4jDriver.close();
    }
}
