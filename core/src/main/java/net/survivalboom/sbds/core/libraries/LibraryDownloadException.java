package net.survivalboom.sbds.core.libraries;

public class LibraryDownloadException extends Exception {

    public LibraryDownloadException(String message) {
        super(message);
    }

    public LibraryDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

    public LibraryDownloadException(Throwable cause) {
        super(cause);
    }

}
