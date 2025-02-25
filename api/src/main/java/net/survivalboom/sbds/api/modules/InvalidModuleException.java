package net.survivalboom.sbds.api.modules;

public class InvalidModuleException extends Exception {

    public InvalidModuleException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidModuleException(String message) {
        super(message);
    }

    public InvalidModuleException(Throwable t) {
        super(t.getMessage(), t);
    }

}
