package net.survivalboom.sbds.api.modules;

public class ModuleLoadingException extends Exception {

    public ModuleLoadingException(String message, Throwable cause) {
        super(message, cause);
    }

    public ModuleLoadingException(String message) {
        super(message);
    }

    public ModuleLoadingException(Throwable t) {
        super(t.getMessage(), t);
    }

}
