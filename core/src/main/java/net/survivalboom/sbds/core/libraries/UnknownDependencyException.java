package net.survivalboom.sbds.core.libraries;

import org.jetbrains.annotations.NotNull;

public class UnknownDependencyException extends Exception {

    private final LibrarySearchInfo info;

    public UnknownDependencyException(String message, LibrarySearchInfo info) {
        super(message);
        this.info = info;
    }

    public @NotNull LibrarySearchInfo searchInfo() {
        return info;
    }

}
