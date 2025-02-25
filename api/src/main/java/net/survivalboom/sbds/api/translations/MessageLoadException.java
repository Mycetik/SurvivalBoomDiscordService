package net.survivalboom.sbds.api.translations;

public class MessageLoadException extends Exception {

    private final String key;

    public MessageLoadException(String key, String msg) {
        super(msg);
        this.key = key;
    }

    public MessageLoadException(String key, Throwable cause) {
        super("Failed to load message `" + key + "`: " + cause, cause);
        this.key = key;
    }

    public String getKey() {
        return key;
    }

}
