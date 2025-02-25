package net.survivalboom.sbds.api.commands.argument;

public class ArgumentParseException extends Exception {

    public ArgumentParseException(String message) {
        super(message);
    }

    public ArgumentParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArgumentParseException(Throwable cause) {
        super(cause);
    }

    public ArgumentParseException() {
        super();
    }

}
