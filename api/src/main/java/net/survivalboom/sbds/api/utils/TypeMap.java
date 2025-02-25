package net.survivalboom.sbds.api.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class TypeMap {

    private final Map<String, Object> map = new HashMap<>();

    public TypeMap(@NotNull Map<String, Object> map) {
        this.map.putAll(map);
    }

    public TypeMap() {}

    public @Nullable Object getByName(@NotNull String name) {
        Objects.requireNonNull(name, "name == null");
        return map.get(name);
    }

    public @Nullable <T> T getByName(@NotNull String name, @NotNull Class<T> type) {

        Objects.requireNonNull(type, "type == null");

        Object object = getByName(name);
        if (object == null) return null;

        return type.cast(object);

    }

    public @NotNull <T> T getByName(@NotNull String name, @NotNull T orElse) {

        Objects.requireNonNull(orElse, "orElse == null");

        Object object = getByName(name);
        if (object == null) {
            return orElse;
        }

        try {
            return (T) object;
        }

        catch (ClassCastException e) {
            return orElse;
        }

    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }

    public boolean contains(@NotNull String name) {
        return map.containsKey(name);
    }


    public @NotNull Map<String, Object> map() {
        return new HashMap<>(map);
    }

}
