package net.survivalboom.sbds.api.libraries;

public class RepositoryConnectionException extends Exception {

    public RepositoryConnectionException(String message) {
        super(message);
    }

    public RepositoryConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

    public RepositoryConnectionException(Throwable e) {
        super(e);
    }

}
