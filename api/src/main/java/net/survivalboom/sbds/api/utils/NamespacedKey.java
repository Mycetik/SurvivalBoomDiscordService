package net.survivalboom.sbds.api.utils;

import net.survivalboom.sbds.api.modules.IModule;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class NamespacedKey {

    public static String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyz_1234567890";

    private final String prefix;

    private final String key;

    private NamespacedKey(@NotNull String prefix, @NotNull String key) {
        this.prefix = prefix;
        this.key = key;
    }

    public @NotNull String prefix() {
        return prefix;
    }

    public @NotNull String key() {
        return key;
    }

    @Override
    public String toString() {
        return prefix + ":" + key;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj instanceof NamespacedKey n) {
            return n.key.equals(this.key) && n.prefix.equals(this.prefix);
        }

        return false;

    }

    @Override
    public int hashCode() {
        return Objects.hash(prefix, key);
    }

    public static @NotNull NamespacedKey fromModule(@NotNull IModule module, @NotNull String key) {

        Objects.requireNonNull(module, "module == null");
        Objects.requireNonNull(key, "value == null");

        if (!checkFormat(key)) throw new IllegalArgumentException("Key contains illegal characters. Allowed characters: " + String.join(" ", ALLOWED_CHARACTERS));

        String prefix = module.getName().toLowerCase();

        return new NamespacedKey(prefix, key);

    }

    public static @NotNull NamespacedKey sbds(@NotNull String key) {

        Objects.requireNonNull(key, "value == null");

        if (!checkFormat(key)) throw new IllegalArgumentException("Key contains illegal characters. Allowed characters: " + String.join(" ", ALLOWED_CHARACTERS));

        return new NamespacedKey("sbds", key);

    }

    public static @NotNull NamespacedKey fromString(@NotNull String str) {

        Objects.requireNonNull(str, "str == null");

        if (!str.contains(":")) throw new IllegalArgumentException("Invalid format. Example: `testmodule:my_key`");

        String[] args = str.split(":");
        if (args.length != 2) throw new IllegalArgumentException("Invalid format. Example: `testmodule:my_key`");

        String prefix = args[0];
        String key = args[1];

        if (!checkFormat(prefix)) throw new IllegalArgumentException("Prefix contains illegal characters. Allowed characters: " + String.join(" ", ALLOWED_CHARACTERS));
        if (!checkFormat(key)) throw new IllegalArgumentException("Key contains illegal characters. Allowed characters: " + String.join(" ", ALLOWED_CHARACTERS));

        return new NamespacedKey(prefix, key);

    }

    public static boolean checkFormat(@NotNull String input) {

        for (char c : input.toCharArray()) {
            if (ALLOWED_CHARACTERS.indexOf(c) == -1) return false;
        }

        return true;

    }

}
