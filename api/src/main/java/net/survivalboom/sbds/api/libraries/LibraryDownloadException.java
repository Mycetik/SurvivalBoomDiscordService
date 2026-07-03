package net.survivalboom.sbds.api.libraries;

public class LibraryDownloadException extends Exception {

    public LibraryDownloadException(String message) {
        super(message);
    }

    public LibraryDownloadException(String message, Throwable cause) {
        super(message, cause);
    }

}
