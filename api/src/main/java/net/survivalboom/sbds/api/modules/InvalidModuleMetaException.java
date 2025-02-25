package net.survivalboom.sbds.api.modules;

public class InvalidModuleMetaException extends Exception {

    public InvalidModuleMetaException(String message) {
        super(message);
    }

    public InvalidModuleMetaException(String message, Throwable cause) {
        super(message, cause);
    }

    public InvalidModuleMetaException(Throwable cause) {
        super(cause.getMessage(), cause);
    }

}
