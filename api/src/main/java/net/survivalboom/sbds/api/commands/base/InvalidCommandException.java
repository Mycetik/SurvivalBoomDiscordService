package net.survivalboom.sbds.api.commands.base;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Method;

public class InvalidCommandException extends RuntimeException {

    public InvalidCommandException(String message) {
        super(message);
    }

    public InvalidCommandException(String message, Throwable cause) {
        super(message, cause);
    }


    public static @NotNull InvalidCommandException createInvalidArgumentException(@NotNull Method method, @NotNull String message, @Nullable Throwable cause) {
        return new InvalidCommandException(String.format("Invalid command argument %s: %s", method.getName(), message), cause);
    }

}
