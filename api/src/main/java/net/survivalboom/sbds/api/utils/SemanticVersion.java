package net.survivalboom.sbds.api.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public record SemanticVersion(int major, int minor, int patch) {

    public SemanticVersion {

        if (major < 0) {
            throw new IllegalArgumentException("major < 0");
        }

        if (minor < 0) {
            throw new IllegalArgumentException("minor < 0");
        }

        if (patch < 0) {
            throw new IllegalArgumentException("patch < 0");
        }

    }


    public static @NotNull SemanticVersion fromString(@NotNull String string) {

        Objects.requireNonNull(string, "string == null");
        if (string.isBlank()) {
            throw new IllegalArgumentException("String is blank!");
        }

        int major;
        int minor = 0;
        int patch = 0;

        String[] parts = string.split("\\.");
        if (parts.length == 0 || parts.length == 1) {
            major = parseNumber(string, "MAJOR");
        }

        else if (parts.length == 2) {
            major = parseNumber(parts[0], "MAJOR");
            minor = parseNumber(parts[1], "MINOR");
        }

        else {
            major = parseNumber(parts[0], "MAJOR");
            minor = parseNumber(parts[1], "MINOR");
            patch = parseNumber(parts[2], "PATCH");
        }

        return new SemanticVersion(major, minor, patch);

    }

    private static int parseNumber(@NotNull String str, @NotNull String target) {

        try {
            return Integer.parseInt(str);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid format for `" + target + "` must be a number. Got: `" + str + "`");
        }

    }

}
