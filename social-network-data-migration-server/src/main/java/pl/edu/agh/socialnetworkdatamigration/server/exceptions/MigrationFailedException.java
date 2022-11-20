package pl.edu.agh.socialnetworkdatamigration.server.exceptions;

public class MigrationFailedException extends RuntimeException {
    public MigrationFailedException(Exception e) {
        super(e);
    }
}
