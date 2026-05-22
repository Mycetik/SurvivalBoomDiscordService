package net.survivalboom.sbds.api.utils;

import net.survivalboom.sbds.api.modules.IModule;
import net.survivalboom.sbds.api.modules.ModuleMain;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class NamespacedKey {

    public static final String ALLOWED_CHARACTERS = "abcdefghijklmnopqrstuvwxyz_.1234567890";

    private static final WeakHashMap<String, NamespacedKey> keys = new WeakHashMap<>();


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


    public static @NotNull NamespacedKey create(@NotNull String prefix, @NotNull String key) {

        Objects.requireNonNull(prefix, "prefix == null");
        Objects.requireNonNull(key, "key == null");

        String prefix0 = prefix.toLowerCase();
        String key0 = key.toLowerCase().replace("-", "_");

        if (!checkFormat(prefix0)) {
            throw new IllegalArgumentException("Prefix `" + prefix0 + "` contains illegal characters. Allowed characters: " + String.join(" ", ALLOWED_CHARACTERS));
        }

        if (!checkFormat(key0)) {
            throw new IllegalArgumentException("Key `" + key0 + "` contains illegal characters. Allowed characters: " + String.join(" ", ALLOWED_CHARACTERS));
        }

        String plus = prefix0 + ":" + key0;

        return keys.computeIfAbsent(plus, k -> new NamespacedKey(prefix0, key0));

    }

    public static @NotNull NamespacedKey fromModule(@NotNull ModuleMain moduleMain, @NotNull String key) {

        Objects.requireNonNull(moduleMain, "moduleMain == null");

        return fromModule(moduleMain.getModule(), key);

    }

    public static @NotNull NamespacedKey fromModule(@NotNull IModule module, @NotNull String key) {

        Objects.requireNonNull(module, "module == null");
        Objects.requireNonNull(key, "value == null");

        return create(module.getName().toLowerCase(), key);

    }

    public static @NotNull NamespacedKey sbds(@NotNull String key) {
        Objects.requireNonNull(key, "key == null");
        return create("sbds", key);
    }

    public static @NotNull NamespacedKey fromString(@NotNull String str) {

        Objects.requireNonNull(str, "str == null");

        if (!str.contains(":")) throw new IllegalArgumentException("Invalid format. Example: `testmodule:my_key`. Got `" + str + "`");

        String[] args = str.split(":");
        if (args.length != 2) throw new IllegalArgumentException("Invalid format. Example: `testmodule:my_key`");

        String prefix = args[0];
        String key = args[1];

        return create(prefix, key);

    }

    public static boolean checkFormat(@NotNull String input) {

        for (char c : input.toCharArray()) {
            if (ALLOWED_CHARACTERS.indexOf(c) == -1) return false;
        }

        return true;

    }

}
