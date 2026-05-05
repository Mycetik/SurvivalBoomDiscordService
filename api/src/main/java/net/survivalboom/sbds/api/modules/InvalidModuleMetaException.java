package net.survivalboom.sbds.api.modules;

public class InvalidModuleMetaException extends Exception {

    public InvalidModuleMetaException(String message) {
        super(message);
    }

    public InvalidModuleMetaException(Throwable cause) {
        super(cause);
    }

}
